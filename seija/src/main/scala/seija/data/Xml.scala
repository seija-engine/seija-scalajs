package seija.data

import seija.core.Seija

import scala.scalajs.js


trait XmlNode extends js.Object {
  val tag:String
  val attrs:js.Dictionary[String];
  val children:js.UndefOr[js.Array[XmlNode]];
}

object Xml {
  def fromString(str:String):Either[String,XmlNode] = xmlRetToEither(Seija.parseXMLString(str))

  def fromFile(path:String):Either[String,XmlNode] = xmlRetToEither(Seija.parseXML(path))

  private def xmlRetToEither(loadData:js.Any):Either[String,XmlNode] = {
    if(js.typeOf(loadData) == "string") {
      Left(loadData.asInstanceOf[String])
    } else {
      Right(loadData.asInstanceOf[XmlNode])
    }
  }
}

object XmlExt {
  implicit class RichXmlNode(node: XmlNode) {
    def toJsonString:String = js.JSON.stringify(node)

    def searchTagNode(tagName:String):js.Array[XmlNode] = _searchTagNode(tagName,node)


    private def _searchTagNode(tagName:String,node:XmlNode):js.Array[XmlNode] = {
      var arr:js.Array[XmlNode] = js.Array();
      if(tagName == node.tag) {
        arr.push(node);
      }
      if(node.children.isDefined) {
        for(idx <- 0 until node.children.get.length) {
          var xmlNode:XmlNode = node.children.get(idx);
          var nodes = _searchTagNode(tagName,xmlNode)
          arr = arr.concat(nodes);
        }
      }
      arr
    }
  }
}