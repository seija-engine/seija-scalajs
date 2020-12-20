package seija.ui2.controls
import seija.ui2.Control
import scala.scalajs.js.Dictionary

class StackLayout extends Control {
  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
  }
}
