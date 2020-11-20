package seija.ui2
import seija.core.Entity
import seija.data.{Read, SBool, SContent, SExpr, SExprInterp, SFloat, SFunc, SInt, SKeyword, SList, SNFunc, SNil, SObject, SString, SSymbol, SUserData, SVector, Xml, XmlNode}
import seija.math.Vector2

import scalajs.js

trait UIComponent {
  def attach(entity: Entity,xmlNode:XmlNode,tmpl:UITemplate):Unit
}

object UISystem {
  var rootPath:String = ""
  private val controlCreators:js.Dictionary[XmlNode => Either[String,Control]] = js.Dictionary()
  private val comps:js.Dictionary[UIComponent] = js.Dictionary()
  private var controlContent:Option[SContent] = None
  def cContent:Option[SContent] = controlContent

  def initCore():Unit = {
    this.registerControl("ImageControl",ImageControl.create)

    this.registerComp("Transform",new TransformUIComp)
    this.registerComp("Rect2D",new Rect2DUIComp)
    this.registerComp("ImageRender",new ImageRenderUIComp)
    this.registerComp("Transparent",new TransparentUIComp)
    this.registerComp("EventNode",new EventNodeUIComp)

    val content = new SContent(Some(SExprInterp.rootContent))
    this.controlContent = Some(content)
    content.set("attr", SNFunc(UISystemSFunc.attr))
    content.set("emit",SNFunc(UISystemSFunc.emit))
    content.set("attr-bind",SNFunc(UISystemSFunc.attrBind))
    content.set("ev-bind",SNFunc(UISystemSFunc.evBind))
  }

  def create(path:String):Either[String, Control] = {
    val filePath = rootPath + path
    for {
      xmlNode <- Xml.fromFile(filePath)
      createFn <- this.controlCreators.get(xmlNode.tag).toRight(s"not found control creator $xmlNode.tag")
      control <- createFn(xmlNode)
    } yield control
  }

  def registerControl(name:String,createFn:XmlNode => Either[String,Control]):Unit = {
    this.controlCreators.put(name,createFn)
  }

  def registerComp(compName: String,comp:UIComponent):Unit = {
    this.comps.put(compName,comp)
  }

  def getUIComp(name:String):Option[UIComponent] = {
    this.comps.get(name)
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