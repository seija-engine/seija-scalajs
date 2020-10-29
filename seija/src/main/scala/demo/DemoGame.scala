package demo
import math.Vector3
import core.{App, Entity, IGame, Template, Time, Transform, TransformTmpl}
import s2d.{ImageFilled, ImageRender, ImageRenderTmpl, Rect2D, Rect2DTmpl, SpriteRender, TextRender, TextRenderTmpl, Transparent, TransparentTmpl}
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
    Template.registerComponent(new Rect2DTmpl)
    Template.registerComponent(new TransparentTmpl)
    Template.registerComponent(new TextRenderTmpl)
    Template.registerComponent(new ImageRenderTmpl)

    var eTmpl = Template.fromXmlFile("/label.xml");
    println("font:"+font.toString);
    for {
      tmpl <- eTmpl
    } {
      val entity = tmpl.call(js.Dictionary(
        "font" -> font.id,
        "position" -> Vector3.New(10,10,0),
        "scale" -> Vector3.New(1,1,1)
      ))
      println(entity)

    }



  }





  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
