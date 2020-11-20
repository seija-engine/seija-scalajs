package seija.ui2

import seija.data.{SExpr, SList}

import scala.scalajs.js

trait IBehavior {
  def handleEvent(evData:js.Array[SExpr]):Unit = {}

  def emit(evKey:String,evData:SExpr):Unit = {}
}
