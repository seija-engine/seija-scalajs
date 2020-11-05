package core
import core.event.EventSystem

import scala.scalajs.js
import s2d.Simple2d

import scala.scalajs.js.annotation._

trait IGame {
  def onStart()
  def onUpdate()
  def onQuit()
}

class App(val game:IGame,val simple2d: Simple2d) {
  def run(): Unit = {
    Foreign.initDeno()
    val s = Foreign.newSimple2d(simple2d.toJS);
    Seija.runApp(s,this.onStart,this.onUpdate,this.onQuit);
  }

  def onStart:js.Function1[Int,Unit] = (worldId) => {
    World.init(worldId)
    this.game.onStart()
  }

  def onUpdate:js.Function = (events:js.Array[js.Any]) => {
    if(events.length > 0) {
      for(ev <- events) {
         EventSystem.handleEvent(ev.asInstanceOf[js.Array[js.Any]])
      }
    }
    this.game.onUpdate();
  }

  def onQuit:js.Function0[Unit] = () => {
    this.game.onQuit()
  }
}

object App {
  def close():Unit = {
    Foreign.closeApp()
  }
}
