package seija.ui2
import seija.core.Entity
import seija.data.{Read, SBool, SContent, SExpr, SExprInterp, SFloat, SFunc, SInt, SKeyword, SList, SNFunc, SNil, SObject, SString, SSymbol, SUserData, SVector, Xml, XmlNode}
import seija.math.Vector2
import seija.ui2.controls.{CheckBox, ImageControl, SpriteControl}

import scalajs.js

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

    this.registerComp("Transform",new TransformUIComp)
    this.registerComp("Rect2D",new Rect2DUIComp)
    this.registerComp("ImageRender",new ImageRenderUIComp)
    this.registerComp("Transparent",new TransparentUIComp)
    this.registerComp("EventNode",new EventNodeUIComp)
    this.registerComp("SpriteRender",new SpriteRenderUIComp)

    val content = new SContent(Some(SExprInterp.rootContent))
    this.controlContent = Some(content)
    content.set("attr", SNFunc(UISystemSFunc.attr))
    content.set("emit",SNFunc(UISystemSFunc.emit))
    content.set("attr-bind",SNFunc(UISystemSFunc.attrBind))
    content.set("ev-bind",SNFunc(UISystemSFunc.evBind))
    content.set("env",SNFunc(UISystemSFunc.env))
  }

  def create(path:String,args:js.Dictionary[String] = js.Dictionary(),parent:Option[Control] = None):Either[String, Control] = {
    val filePath = rootPath + path
    println("load xml:" + filePath)
    val createByXmlNode:(XmlNode,()=>Control)=> Either[String,Control] = (xmlNode,createF) => {
      if(xmlNode.children.isEmpty) {
        return Left("need children")
      }
      val control = createF()
      control.parent = parent
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
      control.init()
      control.setParams(args)
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
    val ret = control.property.get(attrName).map(SUserData).getOrElse(SNil)
    ret
  }

  def emit(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val control = content.find("control").get.asInstanceOf[SUserData].value.asInstanceOf[Control]
    control.handleEvent(evalArgs)
    SNil
  }

  def attrBind(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val control = content.find("control").get.asInstanceOf[SUserData].value.asInstanceOf[Control]
    val attrName = evalArgs(0).asInstanceOf[SKeyword].value.tail
    val setFunc = content.find("setFunc").get.asInstanceOf[SUserData].value.asInstanceOf[(Any) =>Unit]
    control.property.get(attrName).foreach(v => setFunc(v))
    control.addPropertyListener(attrName,setFunc)
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
    control.addEvent(evName,callFn)
    SNil
  }

  def env(args:js.Array[SExpr],content:SContent):SExpr = {
    val envName = args(0).asInstanceOf[SSymbol].value
    val findValue = UISystem.findEnv(envName)
    SUserData(findValue)
  }
}


object UIComponent {
  val cacheContent:SContent = new SContent()
  def initParam[T](name:String,dic:js.Dictionary[String],setFunc:(T) => Unit,content: SContent)(implicit readT:Read[T]):Unit = {
    dic.get(name).map(Utils.parseParam).foreach {
      case Left(value) =>
        readT.read(value).foreach(setFunc)
      case Right(value) =>
        cacheContent.parent = Some(content)
        cacheContent.set("setFunc",SUserData(setFunc))
        SExprInterp.eval(value, Some(cacheContent)) match {
          case SUserData(value) => setFunc(value.asInstanceOf[T])
          case _ => ()
        }
    }
  }
}