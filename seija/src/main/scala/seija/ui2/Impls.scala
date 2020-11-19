package seija.ui2

import seija.core.{Entity, Transform}
import seija.data.{Color, XmlNode}
import seija.math.{Vector2, Vector3}
import seija.s2d.{ImageRender, Rect2D}
import seija.s2d.assets.Image

import scalajs.js
import seija.ui2.Utils
import seija.data.CoreRead._

class TransformUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val trans = entity.addComponent[Transform]()
    val dic = Utils.getXmlNodeParam(xmlNode)
    UIComponent.initParam[Vector3]("position",dic,trans.localPosition = _,tmpl.control.sContent);
    UIComponent.initParam[Vector3]("scale",dic,trans.scale = _,tmpl.control.sContent);
    UIComponent.initParam[Vector3]("rotation",dic,trans.rotation = _,tmpl.control.sContent);
  }
}

class Rect2DUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val rect2d = entity.addComponent[Rect2D]()
    val dic = Utils.getXmlNodeParam(xmlNode)
    UIComponent.initParam[Vector2]("size",dic,rect2d.size = _,tmpl.control.sContent)
    UIComponent.initParam[Vector2]("anchor",dic,rect2d.anchor = _,tmpl.control.sContent)
  }
}

class ImageRenderUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val image = entity.addComponent[ImageRender]()
    val dic = Utils.getXmlNodeParam(xmlNode)
    UIComponent.initParam[Int]("texture",dic,tex => image.setTexture(new Image(tex)),tmpl.control.sContent)
    UIComponent.initParam[Color]("color",dic,image.color = _,tmpl.control.sContent)
  }
}