package demo

import core.{App, Entity, IGame, Time, Transform}

class DemoGame extends IGame {
  override def onStart(): Unit = {
    val root = Entity.New()
    val trans:Transform = root.addComponent[Transform]();
    

  }

  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }
}
