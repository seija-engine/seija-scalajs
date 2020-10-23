package demo
import math.Vector3
import core.{App, Entity, IGame, Time, Transform}
import s2d.{ImageFilled, ImageRender, Rect2D, SpriteRender, Transparent}
import assets.Loader
import data.Color
import s2d.assets.{Font, Image, SpriteSheet, TextureConfig}

class DemoGame extends IGame {
  var uiEntity :Entity = null;
  var index:Int = 0;
  var uiT:Transform = null;
  var imageRender:ImageRender =  null;
  override def onStart(): Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    this.uiEntity = Entity.New();
    this.uiEntity.addComponent[Transparent]()
    this.uiT = this.uiEntity.addComponent[Transform]();
    var rect2d = this.uiEntity.addComponent[Rect2D]();
    rect2d.size.x = 100;
    rect2d.size.y = 100;
    this.uiT.localPosition.set(200f,0f,99f)


    var image = Loader.loadSync[Image]("StarIcon.png",Some(new TextureConfig())).toOption.get;
    this.imageRender = this.uiEntity.addComponent[ImageRender]()
    imageRender.setTexture(image)
    imageRender.setImageType(s2d.ImageFilled(s2d.ImageFilledType.HorizontalLeft,0.1f))
    imageRender.setFilledValue(0.45f)
    imageRender.color = Color.New(1f,1f,1f,1f)

    var sheet = Loader.loadSync[SpriteSheet]("material.json",Some(new TextureConfig())).toOption.get;
    this.createSprite(sheet)

    var font = Loader.loadSync[Font]("WenQuanYiMicroHei.ttf").toOption.get;

    println(font);
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
    sprite.color.set(1,1,1,1f)
    sprite.setSliceByConfig(0)

    println(sprite);
  }

  def createLabel():Unit = {

  }


  var fillValue:Float = 0f;
  var dir:Int = 1;
  override def onUpdate(): Unit = {
    this.fillValue += Time.timeDelta() * this.dir * 2;
    if(this.fillValue > 1f) {
      dir = -1;
    }
    if(this.fillValue < 0) {
      dir = 1;
    }
    this.imageRender.setFilledValue(this.fillValue)
  }

  override def onQuit(): Unit = {


  }


}
