package demo
import math.Vector3
import core.{App, Entity, IGame, Time, Transform}

class DemoGame extends IGame {
  var rootT:Transform = null;
  var uiEntity :Entity = null;
  override def onStart(): Unit = {
    val root = Entity.New()
    this.uiEntity = Entity.New();
    uiEntity.setParent(root);
    root.addChildren(this.uiEntity);
    var trans:Transform = root.addComponent[Transform]();

    trans.localPosition.x = 0.12f;

    trans.scale.x = 10f;
    trans.rotation.x = 0.25f;

    println("position:" + trans.localPosition);
    println("scale:" + trans.scale);
    println("rotation:" + trans.rotation);
    this.rootT = trans;

    println(root.childrens)
  }

  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }
}
