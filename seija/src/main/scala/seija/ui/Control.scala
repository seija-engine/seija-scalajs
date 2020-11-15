package seija.ui

import seija.data.{SContent, SExpr, SExprInterp, SExprParser, SNFunc, Xml, XmlNode}
import seija.data.SNil
import scalajs.js;

class Control(private val tmplDic: js.Dictionary[XmlNode]) {
  var contentTemplate: Option[XmlNode] = tmplDic.get("ContentTemplate")

  def Enter(): Unit = {
    if (this.contentTemplate.isDefined) {
      UITemplate.create(this.contentTemplate.get)
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

  def init():Unit = {
    this.sContent.set("env",SNFunc(ControlSFunc.env))
  }
}

private object ControlSFunc {
  def env (args:js.Array[SExpr],content: SContent):SExpr = {
    println("run this fuck")
    SNil
  }
}