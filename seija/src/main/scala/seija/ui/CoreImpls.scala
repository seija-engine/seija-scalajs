package seija.ui

import seija.core.Entity
import seija.data.{Color, XmlNode}
import seija.math.Vector3
import seija.core.Transform
import seija.s2d.ImageRender

class TransformUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val dic = UITemplate.getXmlNodeParam(xmlNode)
    val trans = entity.addComponent[Transform]();
    UITemplate.initParam[Vector3]("position",dic, trans.localPosition = _,tmpl.sContext)
  }
}

class Rect2DUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {

  }
}

class ImageRenderUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val image = entity.addComponent[ImageRender]();
    val dic = UITemplate.getXmlNodeParam(xmlNode)
    UITemplate.initParam[Color]("color",dic,image.color = _,tmpl.sContext)
  }
}