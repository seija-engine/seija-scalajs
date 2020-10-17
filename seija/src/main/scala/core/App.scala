package core
import scala.scalajs.js;
import s2d.Simple2d
trait IGame {
  def onStart()
  def onUpdate()
  def onQuit()
}

class App(val game:IGame,val simple2d: Simple2d) {
  def run(): Unit = {
    Foreign.init_deno()
    val s = Foreign.newSimple2d(simple2d.toJS);
    Seija.runApp(s,this.onStart,this.onUpdate,this.onQuit);
  }

  def onStart:js.Function1[Int,Unit] = (worldId) => {
    World.init(worldId)
    this.game.onStart()
  }

  def onUpdate:js.Function0[Unit] = () => {
    this.game.onUpdate()
  }

  def onQuit:js.Function0[Unit] = () => {
    this.game.onQuit()
  }
}

object App {
  def close():Unit = {
    Foreign.closeApp(World.id)
  }
}
