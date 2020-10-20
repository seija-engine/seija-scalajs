package demo
import math.Vector3
import core.{App, Entity, IGame, Time, Transform}
import s2d.Rect2D
import s2d.assets.{Image, TextureConfig}

class DemoGame extends IGame {
  var rootT:Transform = null;
  var uiEntity :Entity = null;
  var index:Int = 0;
  override def onStart(): Unit = {
    val root = Entity.New()
    this.uiEntity = Entity.New();
    this.uiEntity.addComponent[Transform]();
    this.uiEntity.setParent(Some(root));
    var rect2d = this.uiEntity.addComponent[Rect2D]();

    var e2 = Entity.New();
    e2.setParent(Some(this.uiEntity));
    var t = e2.addComponent[Transform]();
    this.rootT = root.addComponent[Transform]();

    this.uiEntity.destory()

    //assets.Loader.loadSync[Image]("a.png",new TextureConfig);



    println("Alive:"+root.isAlive);

  }


  override def onUpdate(): Unit = {

    if(this.index == 2) {

      println(Entity.all());
      println("end");
    }
    this.index+=1

  }

  override def onQuit(): Unit = {


  }

  def createImage(parent:Option[Entity]):Entity = {
    var entity = Entity.New();
    entity
  }
}
