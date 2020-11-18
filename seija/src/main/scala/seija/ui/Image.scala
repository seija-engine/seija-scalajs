package seija.ui
import seija.data.XmlNode

import scala.scalajs.js

class Image(private val xmlNode: XmlNode) extends Control(xmlNode) {

}

object Image {
  def create(xmlNode: XmlNode):Image = {
    val img = new Image(xmlNode)
    img.init()
    img
  }
}