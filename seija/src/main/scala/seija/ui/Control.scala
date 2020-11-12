package seija.ui

import seija.data.{Xml, XmlNode}
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
}