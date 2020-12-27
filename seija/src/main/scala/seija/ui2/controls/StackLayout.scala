package seija.ui2.controls
import seija.data.CoreRead.floatRead
import seija.s2d.layout.Orientation
import seija.s2d.layout.Orientation.Orientation
import seija.ui2.Control

import scala.scalajs.js.Dictionary

class StackLayout extends Control {
  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
    this.setParam[Orientation]("orientation",params,Some(Orientation.Horizontal))
    this.setParam[Float]("spacing",params,Some(0))
  }
}
