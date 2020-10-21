package demo
import math.Vector3
import core.{App, Entity, IGame, Time, Transform}
import s2d.{ImageRender, Rect2D}
import assets.Loader;
import s2d.assets.{Image, TextureConfig}

class DemoGame extends IGame {
  var uiEntity :Entity = null;
  var index:Int = 0;
  var uiT:Transform = null;
  override def onStart(): Unit = {
    val root = Entity.New()
    this.uiEntity = Entity.New();
    this.uiT = this.uiEntity.addComponent[Transform]();
    var rect2d = this.uiEntity.addComponent[Rect2D]();
    rect2d.size.x = 100;
    rect2d.size.y = 100;
    println(rect2d.anchor);

    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")

    Loader.loadSync[Image]("StarIcon.png",new TextureConfig()) match {
      case Right(image) =>
        var imageRender = this.uiEntity.addComponent[ImageRender]()
        imageRender.setTexture(image)
        imageRender.setImageType(s2d.ImageSimple)
        imageRender.color.a = 0.5f
      case Left(err) => println(err)
    }

  }


  override def onUpdate(): Unit = {
    //this.uiT.localPosition.x += 1
  }

  override def onQuit(): Unit = {


  }

  def createImage(parent:Option[Entity]):Entity = {
    var entity = Entity.New();
    entity
  }
}
