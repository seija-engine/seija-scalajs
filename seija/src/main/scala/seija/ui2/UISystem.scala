package seija.ui2
import seija.core.Entity
import seija.core.event.{EventNode, GameEventType}
import seija.data.{Read, SBool, SContent, SExpr, SExprInterp, SFloat, SFunc, SInt, SKeyword, SList, SNFunc, SNil, SObject, SString, SSymbol, SUserData, SVector, Xml, XmlNode}
import seija.math.Vector2
import seija.ui2.controls.{CheckBox, ImageControl, Panel, SpriteControl}

import scalajs.js
import seija.ui2.controls.ListView
import scala.collection.immutable.HashMap

trait UIComponent {
  def attach(entity: Entity,xmlNode:XmlNode,tmpl:UITemplate):Unit
}

object UISystem {
  def cContent:Option[SContent] = controlContent
  var rootPath:String = ""
  private val controlCreators:js.Dictionary[() => Control] = js.Dictionary()
  private val comps:js.Dictionary[UIComponent] = js.Dictionary()
  private var controlContent:Option[SContent] = None

  val env:js.Dictionary[Any] = js.Dictionary()

  def initCore():Unit = {
    this.registerControl("ImageControl",() => new ImageControl)
    this.registerControl("SpriteControl",() => new SpriteControl)
    this.registerControl("CheckBox",() => new CheckBox)
    this.registerControl("Panel",() => new Panel)
    this.registerControl("ListView",() => new ListView)

    this.registerComp("Transform",new TransformUIComp)
    this.registerComp("Rect2D",new Rect2DUIComp)
    this.registerComp("ImageRender",new ImageRenderUIComp)
    this.registerComp("Transparent",new TransparentUIComp)
    this.registerComp("EventNode",new EventNodeUIComp)
    this.registerComp("SpriteRender",new SpriteRenderUIComp)
    this.registerComp("EventBoard",new EventBoardUIComp)

    val content = new SContent(Some(SExprInterp.rootContent))
    this.controlContent = Some(content)
    content.set("attr", SNFunc(UISystemSFunc.attr))
    content.set("emit",SNFunc(UISystemSFunc.emit))
    content.set("attr-bind",SNFunc(UISystemSFunc.attrBind))
    content.set("ev-bind",SNFunc(UISystemSFunc.evBind))
    content.set("env",SNFunc(UISystemSFunc.env))
    content.set("node-ev-bind",SNFunc(UISystemSFunc.nodeEvBind))
  }

  def create(path:String,args:js.Dictionary[String] = js.Dictionary(),
                         parent:Option[Control] = None,
                         childNodes:js.Array[XmlNode] = js.Array()):Either[String, Control] = {
    val filePath = rootPath + path
    println("load xml:" + filePath)
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
      for(node <- xmlNode.children.get) {
        if(node.tag == "Template") {
          control.template = Some(new UITemplate(node,control))
        }
      }
      val tmplDic = childNodes.filter(_.tag.endsWith("Template")).foldLeft[js.Dictionary[XmlNode]](js.Dictionary())((d,x) => {
        d.put(x.tag,x)
        d
      })
      control.setParams(args)
      control.setTemplates(tmplDic)
      control.init()
      
      control.OnEnter()
      Right(control)
    }
    
    for {
      xmlNode <- Xml.fromFile(filePath)
      createFn <- this.controlCreators.get(xmlNode.tag).toRight(s"not found control creator ${xmlNode.tag}")
      control <- createByXmlNode(xmlNode,createFn)
    } yield control
  }

  def registerControl(name:String,createFn:()=>Control):Unit = {
    this.controlCreators.put(name,createFn)
  }

  def registerComp(compName: String,comp:UIComponent):Unit = {
    this.comps.put(compName,comp)
  }

  def getUIComp(name:String):Option[UIComponent] = {
    this.comps.get(name)
  }

  def findEnv(name:String):Any = {
    val nameArrays = name.split('.')
    var curValue:Any = env
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
    ret
  }

  def emit(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val control = content.find("control").get.asInstanceOf[SUserData].value.asInstanceOf[Control]

    control.eventBoard.foreach(_.fire(evalArgs.head.castKeyword(),SNil))
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
    val callFn:(SExpr => Unit) = if(evalArgs.length == 3) {
      (evData:SExpr) => {
        val sFunc = evalArgs(2).asInstanceOf[SFunc]
        val callContent = new SContent(Some(content))
        callContent.set("%",evData)
        val evalValue = SExprInterp.evalToValue(SList(sFunc.list),Some(callContent))
        setFunc(evalValue)
      }
    } else {
      (evData:SExpr) => {
        val setValue = SExprInterp.exprToValue(evData)
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
      val evType = GameEventType.gameEventTypeRead.read(evName).get
      eventNode.register(evType,evValue.get._1,evValue.get._2)
    }
    SNil
  }
}


object UIComponent {
  val cacheContent:SContent = new SContent()
  def initParam[T](name:String,dic:js.Dictionary[String],setFunc:(T) => Unit,content: SContent)(implicit readT:Read[T]):Unit = {
    dic.get(name).map(Utils.parseParam).foreach {
      case Left(value) =>
        readT.read(value).foreach(setFunc)
      case Right(expr) =>
        cacheContent.clear()
        cacheContent.parent = Some(content)
        cacheContent.set("setFunc",SUserData(setFunc))
        SExprInterp.eval(expr, Some(cacheContent)) match {
          case SUserData(value) => setFunc(value.asInstanceOf[T])
          case _ => ()
        }
    }
  }
}