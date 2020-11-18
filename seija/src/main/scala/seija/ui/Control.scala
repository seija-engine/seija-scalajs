package seija.ui
import seija.data.{SContent, SExpr, SExprInterp, SExprParser, SKeyword, SNFunc, SNil, SSymbol, SUserData, Xml, XmlNode}

import scalajs.js;

class Control(private val xmlNode: XmlNode) {
  val propertyDic:js.Dictionary[Any] = js.Dictionary()
  val sContent:SContent = new SContent(Some(Control.sContent))
  var tmplDic:js.Dictionary[XmlNode] = js.Dictionary()
  var contentTemplate: Option[XmlNode] = None

  def init():Unit = {
    this.sContent.set("control",SUserData(this))
    xmlNode.children.foreach(_.filter(_.tag.endsWith("Template"))
      .foldLeft[js.Dictionary[XmlNode]](this.tmplDic)((dic, node) => {
        dic.put(node.tag, node)
        dic
      }))
    this.contentTemplate = this.tmplDic.get("ContentTemplate")
    val attrs = xmlNode.attrs.get("attrs")
    if(attrs.isDefined) {
      val attrList = SExprInterp.evalStringToValue(attrs.get,Some(this.sContent))
      if(attrList != null) {
        val nameList = attrList.asInstanceOf[js.Array[String]]
        for(name <- nameList) {
          this.propertyDic.put(name,null)
        }
      }
    }
  }

  def Enter(): Unit = {
    if (this.contentTemplate.isDefined) {
      UITemplate.create(this.contentTemplate.get,this.sContent)
    }
  }
}

object Control {
  val sContent = new SContent(Some(SExprInterp.rootContent))
  private var _rootPath: String = "";
  def rootPath: String = _rootPath
  def setRootPath(path: String): Unit = _rootPath = path

  private var _env:js.Dictionary[Any] = js.Dictionary()
  def env:js.Dictionary[Any] = _env

  private val controlDic:js.Dictionary[(XmlNode) => Control] = js.Dictionary()

  def init():Unit = {
    this.sContent.set("env",SNFunc(ControlSFunc.env))
    this.sContent.set("ev-bind",SNFunc(ControlSFunc.eventBind))
    this.sContent.set("bind",SNFunc(ControlSFunc.bind))
  }

  def create(controlPath: String,controlName:String): Either[String, Control] = {
    val xmlPath = rootPath + controlPath;
    for {
      xmlNode <- Xml.fromFile(xmlPath)
      control <- fromXML(xmlNode,controlName)
    } yield control
  }

  def fromXML(xmlNode: XmlNode,name:String): Either[String, Control] = {
    if (xmlNode.tag != "Control") {
      return Left("root need Control")
    }
    val mayCreateFn = this.controlDic.get(name)
    mayCreateFn match {
      case Some(f) => Right(f(xmlNode))
      case None => Left(s"$name not register")
    }
  }

  def register(name:String, createFn:(XmlNode) => Control):Unit = {
    this.controlDic.put(name,createFn)
  }
}

private object ControlSFunc {
  def env (args:js.Array[SExpr],content: SContent):SExpr = {
    if(args.isEmpty) return  SNil
    val nameSeq = args(0).asInstanceOf[SSymbol].value.split('.')
    var curValue:js.Any = Control.env;
    for(name <- nameSeq) {
      curValue.asInstanceOf[js.Dictionary[js.Any]].get(name) match {
        case Some(value) =>
          curValue = value
        case None => return SNil
      }
    }
    SUserData(curValue)
  }

  def eventBind(args:js.Array[SExpr],content: SContent):SExpr = {
    val uiTemplate = content.find("tmpl").get.asInstanceOf[SUserData].value.asInstanceOf[UITemplate]
    val eventKey = args(0).asInstanceOf[SKeyword].value
    uiTemplate.registerEvent(eventKey)
    SNil
  }

  def bind(args:js.Array[SExpr],content: SContent):SExpr = {
    println(args)
    SNil
  }
}