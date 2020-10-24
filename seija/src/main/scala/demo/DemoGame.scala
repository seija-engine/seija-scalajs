package demo
import math.Vector3
import core.{App, Entity, IGame, Time, Transform}
import s2d.{ImageFilled, ImageRender, Rect2D, SpriteRender, TextRender, Transparent}
import assets.Loader
import data.Color
import s2d.assets.{Font, Image, SpriteSheet, TextureConfig}
import data.XmlExt._;


class DemoGame extends IGame {

  override def onStart(): Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    //BaseRenderComps.show()
    var xml = data.Xml.fromString("<Entity> </Entity>")
    println(xml.toJsonString )
  }





  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
