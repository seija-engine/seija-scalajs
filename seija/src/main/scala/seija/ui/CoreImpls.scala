package seija.ui

import seija.core.Entity
import seija.data.XmlNode
import seija.data.SExprInterp
import seija.math.Vector3
import seija.core.Transform

class TransformUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode): Unit = {
    val trans = entity.addComponent[Transform]();
    val position = xmlNode.attrs.get("position").map(UITemplate.parseParam)
    if(position.isDefined) {
      position.get match {
        case Left(value) =>
          val pos = Vector3.vector3Read.read(value)
          if(pos.isDefined) {
            trans.localPosition = pos.get
          }
        case Right(value) =>
          val evalValue = SExprInterp.eval(value)
          println(value)
      }
    }

  }
}

class Rect2DUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode): Unit = {

  }
}

class ImageRenderUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode): Unit = {

  }
}