package seija.ui2.controls
import seija.ui2.Control

import scala.scalajs.js
import seija.data.Read._
import seija.math.Vector3
import seija.s2d.layout.{LayoutAlignment, Thickness, ViewType}
import seija.s2d.layout.LayoutAlignment.LayoutAlignment
import seija.s2d.layout.ViewType.ViewType
object PropertySet {
  def setEvent(control:Control,params:js.Dictionary[String]):Unit = {
    control.setEventParam("OnClick",params)
  }

  def setLayout(control: Control,params:js.Dictionary[String]):Unit = {
    control.setParam[Float]("width",params,Some(0))
    control.setParam[Float]("height",params,Some(0))
    control.setParam[LayoutAlignment]("hor",params,Some(LayoutAlignment.Fill))
    control.setParam[LayoutAlignment]("ver",params,Some(LayoutAlignment.Fill))
    control.setParam[Vector3]("position",params,Some(Vector3.zero))
    control.setParam[Thickness]("margin",params,Some(Thickness(0,0,0,0)))
    control.setParam[Thickness]("padding",params,Some(Thickness(0,0,0,0)))
    control.setParam[ViewType]("viewType",params,Some(ViewType.Static))
  }
}