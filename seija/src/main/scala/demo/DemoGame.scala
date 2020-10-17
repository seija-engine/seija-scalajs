package demo
import core.{App, Entity, IGame, Time, Transform, World}

class DemoGame extends IGame {
  override def onStart(): Unit = {
    val root = Entity.New()
    root.addComponent[Transform]()
    println(root)
  }

  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }
}
