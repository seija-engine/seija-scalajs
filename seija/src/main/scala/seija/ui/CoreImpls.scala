package seija.ui

import seija.core.Entity
import seija.data.{SBool, SExprInterp, SFloat, SFunc, SInt, SKeyword, SList, SNFunc, SNil, SObject, SString, SSymbol, SVector, XmlNode}
import seija.math.Vector3
import seija.core.Transform

class TransformUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val trans = entity.addComponent[Transform]();
    val position = xmlNode.attrs.get("position").map(Control.parseParam)
    if(position.isDefined) {
      position.get match {
        case Left(value) =>
          val pos = Vector3.vector3Read.read(value)
          if(pos.isDefined) {
            trans.localPosition = pos.get
          }
        case Right(value) =>
          val evalValue = SExprInterp.exprToValue(SExprInterp.eval(value,Some(tmpl.sContext)))
          if(evalValue.isInstanceOf[SFunc]) {

          }
          println(value)
      }
    }

  }
}

class Rect2DUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {

  }
}

class ImageRenderUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {

  }
}