package seija.ui2.controls
import seija.data.SExpr
import seija.ui2.Control
import slogging.LazyLogging

import scala.scalajs.js
import scala.scalajs.js.Dictionary

class Menu extends Control with LazyLogging {
  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
  }

  override def handleEvent(evKey: String, evData: js.Array[SExpr]): Unit = {
    evKey match {
      case ":menu" =>

    }
  }
}
