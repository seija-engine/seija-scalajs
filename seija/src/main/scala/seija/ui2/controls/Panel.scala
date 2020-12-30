package seija.ui2.controls
import seija.ui2.{Control, ControlCreator}

import scala.scalajs.js.Dictionary
class Panel extends Control {
  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
  }
}

object Panel {
  implicit val panelCreator:ControlCreator[Panel] = new ControlCreator[Panel] {
    override def name: String = "Panel"
    override def create(): Control = new Panel
    override def init(): Unit = {}
  }
}