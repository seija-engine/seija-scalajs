package data
import scala.scalajs.js;
import core.Seija
trait XmlNode extends js.Object {
  val tag:String
  val attrs:js.Dictionary[String];
  val children:js.Array[XmlNode];
}

object Xml {
  def fromString(str:String):XmlNode = Seija.parseXMLString(str).asInstanceOf[XmlNode]
}

object XmlExt {
  implicit class RichXmlNode(node: XmlNode) {
    def toJsonString:String = js.JSON.stringify(node)
  }
}