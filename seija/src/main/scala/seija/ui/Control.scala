package seija.ui
import seija.data.{SContent, SExpr, SExprInterp, SExprParser, SKeyword, SNFunc, SNil, SSymbol, SUserData, Xml, XmlNode}

import scalajs.js;

class Control(private val tmplDic: js.Dictionary[XmlNode]) {
  val sContent:SContent = new SContent(Some(Control.sContent))
  var contentTemplate: Option[XmlNode] = tmplDic.get("ContentTemplate")

  def Enter(): Unit = {
    this.sContent.set("control",SUserData(this))
    if (this.contentTemplate.isDefined) {
      UITemplate.create(this.contentTemplate.get,this.sContent)
    }
    println(tmplDic.keys)
    println(contentTemplate)
  }
}

object Control {
  val sContent = new SContent(Some(SExprInterp.rootContent))
  private var _rootPath: String = "";
  def rootPath: String = _rootPath
  def setRootPath(path: String): Unit = _rootPath = path

  private var _env:js.Dictionary[Any] = js.Dictionary()
  def env:js.Dictionary[Any] = _env

  def init():Unit = {
    this.sContent.set("env",SNFunc(ControlSFunc.env))
    this.sContent.set("ev-bind",SNFunc(ControlSFunc.eventBind))
  }

  def create(controlPath: String): Either[String, Control] = {
    val xmlPath = rootPath + controlPath;
    for {
      xmlNode <- Xml.fromFile(xmlPath)
      control <- fromXML(xmlNode)
    } yield control
  }

  def fromXML(xmlNode: XmlNode): Either[String, Control] = {
    if (xmlNode.tag != "Control") {
      return Left("root need Control")
    }
    val dic = xmlNode.children.map(_.filter(_.tag.endsWith("Template"))
      .foldLeft[js.Dictionary[XmlNode]](js.Dictionary())((dic, node) => {
        dic.put(node.tag, node);
        dic
      })).getOrElse(js.Dictionary());
    Right(new Control(dic))
  }


  def parseParam(string: String):Either[String,SExpr] = {
    if(string.length  == 0) {
      return Left("")
    }
    string.head match {
      case '(' =>
        SExprParser.parse(string) match {
          case Left(value) =>
            println(value)
            Left("error")
          case Right(value) => Right(value)
        }
      case str => Left(string.tail)
    }
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
}