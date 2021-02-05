package seija.core

import scala.scalajs.js
import seija.core.event.EventSystem
import seija.s2d.Simple2d

import scala.scalajs.js.annotation._

trait IGame {
  def onStart()
  def onUpdate()
  def onQuit()
}

class App(val game:IGame,val simple2d: Simple2d) {
  def run(): Unit = {
    Foreign.initDeno()
    val s = Seija.makeSimple2d(simple2d.toJS);
    Screen.init(simple2d.window.width,simple2d.window.height)
    Seija.runApp(s,this.onStart,this.onUpdate,this.onQuit);
  }

  def onStart:js.Function1[js.Object,Unit] = (world) => {
    World.init(world)
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
