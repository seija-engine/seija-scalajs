package seija.ui2

import seija.data.{SExpr, SList}

import scala.scalajs.js
import seija.data.SKeyword

trait IEventReceive {
  def handleEvent(evKey:String,evData:js.Array[SExpr]):Unit = {}
}

trait IBehavior extends IEventReceive {
  val eventReceives:js.Array[IEventReceive] = js.Array()

  def emit(evKey:String,evData:js.Array[SExpr]):Unit = {
    this.eventReceives.foreach(_.handleEvent(evKey,evData))
  }

  def addEventRecv(recv:IEventReceive) {
    this.eventReceives.push(recv)
  }
}
