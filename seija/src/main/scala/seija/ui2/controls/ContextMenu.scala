package seija.ui2.controls
import scala.scalajs.js.Dictionary
import scala.scalajs.js
import seija.ui2.Control
import seija.ui2.ControlCreator

class ContextMenu extends Control {
    override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
  }
}

object ContextMenu {
    implicit val contextMenuCreator:ControlCreator[ContextMenu] = new ControlCreator[ContextMenu] {
        override def name: String = "ContextMenu"
        override def create(): Control = new ContextMenu
        override def init(): Unit = {}
    }
}