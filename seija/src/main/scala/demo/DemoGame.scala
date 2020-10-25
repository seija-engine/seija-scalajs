package demo
import math.Vector3
import core.{App, Entity, IGame, Template, Time, Transform, TransformTmpl}
import s2d.{ImageFilled, ImageRender, Rect2D, SpriteRender, TextRender, Transparent}
import assets.Loader
import data.Color
import s2d.assets.{Font, Image, SpriteSheet, TextureConfig}
import data.XmlExt._

import scala.scalajs.js;

class DemoGame extends IGame {

  override def onStart(): Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    Template.setRootPath("../seija-deno/src/tests/res/tmpl")
    var font = Loader.loadSync[Font]("WenQuanYiMicroHei.ttf").toOption.get;
    Template.registerComponent(new TransformTmpl)

    var eTmpl = Template.fromXmlFile("/label.xml");
    for {
      tmpl <- eTmpl
    } {
      tmpl.call(js.Dictionary(
        "font" -> font.id
      ))
    };

  }





  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
