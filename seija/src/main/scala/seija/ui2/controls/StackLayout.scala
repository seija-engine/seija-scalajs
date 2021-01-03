package seija.ui2.controls
import seija.data.Read.floatRead
import seija.s2d.layout.Orientation
import seija.s2d.layout.Orientation.Orientation
import seija.ui2.{Control, ControlCreator}

import scala.scalajs.js.Dictionary

class StackLayout extends Control {
  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
    this.setParam[Orientation]("orientation",params,Some(Orientation.Horizontal))
    this.setParam[Float]("spacing",params,Some(0))
  }
}

object StackLayout {
  implicit val stackLayoutCreator:ControlCreator[StackLayout] = new ControlCreator[StackLayout] {
    override def name: String = "StackLayout"
    override def create(): Control = new StackLayout
    override def init(): Unit = {}
  }
}