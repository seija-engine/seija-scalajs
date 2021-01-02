package seija.ui2
import seija.core.Entity
import seija.core.event.{EventNode, GameEventType}
import seija.data._
import seija.ui2.controls._

import scalajs.js
import seija.data.DynObject
import seija.s2d.layout.{LConst, LRate}
import slogging.LazyLogging

trait UIComponent {
  def attach(entity: Entity,xmlNode:XmlNode,tmpl:UITemplate):Unit
}

trait ControlCreator[T] {
  def name:String
  def create():Control
  def init():Unit
}

object UISystem extends LazyLogging {
  def cContent:Option[SContent] = controlContent
  protected var rootPath:String = ""
  protected val cacheXml:js.Dictionary[XmlNode] = js.Dictionary()
  private val controlCreators:js.Dictionary[() => Control] = js.Dictionary()
  private val comps:js.Dictionary[UIComponent] = js.Dictionary()
  private var controlContent:Option[SContent] = None

  val ENV: js.Dictionary[Any] = js.Dictionary()

  def initCore(path:String):Unit = {
    rootPath = path


    this.registerComp("Transform",new TransformUIComp)
    this.registerComp("Rect2D",new Rect2DUIComp)
    this.registerComp("ImageRender",new ImageRenderUIComp)
    this.registerComp("Transparent",new TransparentUIComp)
    this.registerComp("EventNode",new EventNodeUIComp)
    this.registerComp("SpriteRender",new SpriteRenderUIComp)
    this.registerComp("EventBoard",new EventBoardUIComp)
    this.registerComp("TextRender",new TextRenderUIComp)
    this.registerComp("LayoutView",new LayoutViewUIComp)
    this.registerComp("StackLayout", new StackLayoutUIComp)
    this.registerComp("ContentView",new ContentViewUIComp)
    this.registerComp("GridLayout",new GridLayoutUIComp)
    this.registerComp("GridCell",new GridCellUIComp)
    this.registerComp("CABEventRoot",new CABEventRootUIComp)

    val content = new SContent(Some(SExprInterp.rootContent))
    this.controlContent = Some(content)
    content.set("attr", SNFunc(UISystemSFunc.attr))
    content.set("emit",SNFunc(UISystemSFunc.emit))
    content.set("attr-bind",SNFunc(UISystemSFunc.attrBind))
    content.set("ev-bind",SNFunc(UISystemSFunc.evBind))
    content.set("env",SNFunc(UISystemSFunc.env))
    content.set("node-ev-bind",SNFunc(UISystemSFunc.nodeEvBind))
    content.set("ctx-data",SNFunc(UISystemSFunc.dataF))
    content.set("num-rate",SNFunc(UISystemSFunc.numberRate))
    content.set("num-const",SNFunc(UISystemSFunc.numberConst))

    this.registerControls()
  }

  private def registerControls():Unit = {
    this.registerCreator[ImageControl]()
    this.registerCreator[SpriteControl]()
    this.registerCreator[CheckBox]()
    this.registerCreator[Panel]()
    this.registerCreator[ListView]()
    this.registerCreator[LabelControl]()
    this.registerCreator[StackLayout]()
    this.registerCreator[Grid]()
    this.registerCreator[GridCell]()
    this.registerCreator[Menu]()
    this.registerCreator[ContextMenu]()
    this.registerCreator[EntityControl]()
  }

  def getXml(path:String):Either[String,XmlNode] = {
    if(this.cacheXml.contains(path)) {
      return Right(this.cacheXml(path))
    }
    val eNode = Xml.fromFile(path);
    eNode.foreach(this.cacheXml.put(path,_))
    eNode
  }

  def create(path:String,args:js.Dictionary[String] = js.Dictionary(),
                         parent:Option[Control] = None,
                         tmpls:js.Dictionary[XmlNode] = js.Dictionary(),
                         dataContent:Option[Any] = None):Either[String, Control] = {
    val filePath = rootPath + path
    logger.trace("UISystem create:" + filePath)
    val createByXmlNode:(XmlNode,()=>Control) => Either[String,Control] = (xmlNode,createF) => {
      if(xmlNode.children.isEmpty || xmlNode.children.get.length == 0) {
        return Left("need children")
      }
      val control = createF()
      control.setParent(parent)
      for((k,nsPath) <- xmlNode.attrs) {
        if(k.startsWith("xmlns:")) {
          val nsName = k.drop(6)
          control.nsDic.put(nsName,nsPath)
        }
      }
      for(node <- xmlNode.children.get; if node.tag.endsWith("Template")) {
        if(!tmpls.contains(node.tag))  tmpls.put(node.tag,node)
      }

      control.dataContent = dataContent
      control.setTemplates(tmpls)
      control.setParams(args)
      control.init()
      
      control.OnEnter()
      Right(control)
    }
    
    for {
      xmlNode <- getXml(filePath)
      createFn <- this.controlCreators.get(xmlNode.tag).toRight(s"not found control creator ${xmlNode.tag}")
      control <- createByXmlNode(xmlNode,createFn)
    } yield control
  }

  def registerCreator[T]()(implicit creator:ControlCreator[T]):Unit = {
    this.controlCreators.put(creator.name,() => creator.create())
    creator.init()
  }

  def registerComp(compName: String,comp:UIComponent):Unit = {
    this.comps.put(compName,comp)
  }

  def getUIComp(name:String):Option[UIComponent] = {
    this.comps.get(name)
  }

  def setSExpr(name:String,expr:SExpr):Unit = {
    this.controlContent.foreach(content => content.set(name,expr))
  }

  def findEnv(name:String):Any = {
    val nameArrays = name.split('.')
    var curValue:Any = ENV
    for(name <- nameArrays) {
      val curDic = curValue.asInstanceOf[js.Dictionary[Any]]
      curDic.get(name) match {
        case Some(value) =>
          curValue = value
        case None =>
          return null
      }
    }
    curValue
  }
}


object UISystemSFunc {
  def attr(args:js.Array[SExpr],content:SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val attrName = evalArgs(0).asInstanceOf[SKeyword].value.tail
    val control = content.find("control").get.asInstanceOf[SUserData].value.asInstanceOf[Control]
    val ret = control.property.get(attrName).map(SExpr.fromAny).getOrElse(SNil)
    //println(s"$attrName = $ret")
    ret
  }

  def emit(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val control = content.find("control").get.asInstanceOf[SUserData].value.asInstanceOf[Control]
    control.handleEvent(evalArgs.head.castKeyword(),evalArgs.tail)
    SNil
  }

  def attrBind(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val control = content.find("control").get.asInstanceOf[SUserData].value.asInstanceOf[Control]
    val setFunc = content.find("setFunc").get.asInstanceOf[SUserData].value.asInstanceOf[(Any) =>Unit]
    val attrName = evalArgs(0).asInstanceOf[SKeyword].value.tail
    if(evalArgs.length == 2) {
      val sFunc = evalArgs(1).asInstanceOf[SFunc]
      if(control.property.contains(attrName)) {
        val callFn = (pValue:Any) => {
           val arg = SExpr.fromAny(pValue)
           val evalExpr = sFunc.callByArgs(js.Array(arg),Some(control.sContent))
           val evalValue = SExprInterp.evalToValue(evalExpr,Some(control.sContent))

           setFunc(evalValue)
        }
        callFn(control.property.get(attrName).get)
        control.addPropertyListener(attrName,callFn)
      }
    } else {
      control.property.get(attrName).foreach(v => setFunc(v))
      control.addPropertyListener(attrName,setFunc)
    }

    SNil
  }

  def evBind(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val evName = evalArgs(0).castKeyword()
    val control = content.find("control").get.asInstanceOf[SUserData].value.asInstanceOf[Control]
    val setFunc = content.find("setFunc").get.asInstanceOf[SUserData].value.asInstanceOf[(Any) =>Unit]
    if(evalArgs.length >= 2) {
      setFunc(evalArgs(1).castSingleAny())
    }
    val callFn = if(evalArgs.length == 3) {
      (k:String,evData:js.Array[SExpr]) => {
        val sFunc = evalArgs(2).asInstanceOf[SFunc]
        val evalValue = sFunc.callByArgs(evData,Some(content))
        setFunc(evalValue)
      }
    } else {
      (k:String,evData:js.Array[SExpr]) => {
        val setValue = SExprInterp.exprToValue(SVector(evData))
        setFunc(setValue)
      }
    }
    control.eventBoard.foreach(board=> {
      board.register(evName,callFn)
    })

    SNil
  }

  def env(args:js.Array[SExpr],content:SContent):SExpr = {
    val envName = args(0).asInstanceOf[SSymbol].value
    val findValue = UISystem.findEnv(envName)
    SUserData(findValue)
  }

  def nodeEvBind(args:js.Array[SExpr], content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val control = content.find("control").get.asInstanceOf[SUserData].value.asInstanceOf[Control]
    val eventNode:EventNode = content.find("event-node").get.asInstanceOf[SUserData].value.asInstanceOf[EventNode]
    val evName = evalArgs(0).castKeyword().tail
    val evValue = control.evProperty.get(evName)
    
    if(evValue.isDefined) {
      val evType = GameEventType.gameEventTypeRead.read(evName.substring(2)).get
      eventNode.register(evType,evValue.get._1,evValue.get._2)
    }
    SNil
  }

  def dataF(args:js.Array[SExpr],content:SContent):SExpr = {
    val control = content.find("control").get.asInstanceOf[SUserData].value.asInstanceOf[Control]
    if(control.dataContent.isEmpty) {
      return SNil
    }
    val dataPath = args(0).asInstanceOf[SSymbol].value
    val retValue = DynObject.findValue(dataPath,control.dataContent.get)
    retValue.map(SUserData).getOrElse(SNil)
  }

  def numberRate(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    SUserData(LRate(evalArgs(0).castFloat()))
  }

  def numberConst(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e, Some(content)))
    SUserData(LConst(evalArgs(0).castFloat()))
  }
}


object UIComponent extends LazyLogging {
  val cacheContent:SContent = new SContent()
  def initParam[T](name:String,dic:js.Dictionary[String],setFunc:(T) => Unit,content: SContent)(implicit readT:Read[T]):Unit = {
    dic.get(name).map(Utils.parseParam).foreach {
      case Left(value) =>
        readT.read(value).foreach(setFunc)
      case Right(expr) =>
        cacheContent.clear()
        cacheContent.parent = Some(content)
        cacheContent.set("setFunc",SUserData(setFunc))
        val evalValue  = SExprInterp.eval(expr, Some(cacheContent))
        val castValue = evalValue.castSingleAny()
        if(castValue != null) {
          setFunc(castValue.asInstanceOf[T])
        }
    }
  }

  def initLispParam[T](name:String,dic:js.Dictionary[String],setFunc:(T) => Unit,content:SContent):Unit = {
    dic.get(name).foreach(paramString => {
      cacheContent.clear()
      cacheContent.parent = Some(content)
      cacheContent.set("setFunc",SUserData(setFunc))
      SExprInterp.evalString(paramString, Some(cacheContent)) match {
        case Left(errString) => logger.info(errString)
        case Right(evalValue) =>
          val castValue = evalValue.castSingleAny()
          if(castValue != null) {
            setFunc(castValue.asInstanceOf[T])
          }
      }
    })
  }
}