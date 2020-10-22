package demo
import math.Vector3
import core.{App, Entity, IGame, Time, Transform}
import s2d.{ImageRender, Rect2D, SpriteRender, Transparent}
import assets.Loader
import data.Color
import s2d.assets.{Image, SpriteSheet, TextureConfig}

class DemoGame extends IGame {
  var uiEntity :Entity = null;
  var index:Int = 0;
  var uiT:Transform = null;
  override def onStart(): Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    this.uiEntity = Entity.New();
    this.uiEntity.addComponent[Transparent]()
    this.uiT = this.uiEntity.addComponent[Transform]();
    var rect2d = this.uiEntity.addComponent[Rect2D]();
    rect2d.size.x = 100;
    rect2d.size.y = 100;
    this.uiT.localPosition.set(200f,0f,99f)


    var image = Loader.loadSync[Image]("StarIcon.png",new TextureConfig()).toOption.get;
    var imageRender = this.uiEntity.addComponent[ImageRender]()
    imageRender.setTexture(image)
    imageRender.setImageType(s2d.ImageSliced(5f,5f,5f,5f))
    imageRender.color = Color.New(1,0,1,0.1f)

    var sheet = Loader.loadSync[SpriteSheet]("material.json",new TextureConfig()).toOption.get;
    this.createSprite(sheet)
  }

  def createSprite(sheet:SpriteSheet):Unit = {
    var entity = Entity.New();
    var t = entity.addComponent[Transform]();
    t.localPosition.set(0f,0f,100f)
    var rect2d = entity.addComponent[Rect2D]();
    rect2d.size.set(100f,100f);
    entity.addComponent[Transparent]();
    var sprite = entity.addComponent[SpriteRender]();
    sprite.setSpriteSheet(sheet)
    sprite.setSpriteName("button")
    sprite.color.set(1,0,0,0.1f)

    println(s"simple: ${s2d.ImageSimple.toJsValue}");
    println(s"Sliced:${s2d.ImageSliced(6f,7f,8f,9f).toJsValue}")
    println(s"Filled: ${s2d.ImageFilled(s2d.ImageFilledType.VerticalBottom,0.7f).toJsValue}")
    println(s"Tiled:${s2d.ImageTiled.toJsValue}")
    println(sprite);
  }



  override def onUpdate(): Unit = {
    //this.uiT.localPosition.x += 1
  }

  override def onQuit(): Unit = {


  }


}
