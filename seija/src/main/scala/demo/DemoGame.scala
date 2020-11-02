package demo
import math.Vector3
import core.{App, Entity, IGame, Template, Time, Transform, TransformTmpl}
import s2d.{ImageFilled, ImageRender, ImageRenderTmpl,SpriteRenderTmpl ,Rect2D, Rect2DTmpl, SpriteRender, TextRender, TextRenderTmpl, Transparent, TransparentTmpl}
import assets.Loader
import data.Color
import s2d.assets.{Font, Image, SpriteSheet, TextureConfig}
import data.XmlExt._

import scala.scalajs.js;

class DemoGame extends IGame {

  override def onStart(): Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    Template.setRootPath("../seija-deno/src/tests/res/tmpl")
    val font = Loader.loadSync[Font]("WenQuanYiMicroHei.ttf").toOption.get;
    val tex = Loader.loadSync[Image]("StarIcon.png").toOption.get;
    Template.registerComponent(new TransformTmpl)
    Template.registerComponent(new Rect2DTmpl)
    Template.registerComponent(new TransparentTmpl)
    Template.registerComponent(new TextRenderTmpl)
    Template.registerComponent(new ImageRenderTmpl)
    Template.registerComponent(new SpriteRenderTmpl)

    val root = Entity.New()
    root.info.name = "Root"
    println(root.info.name)
    /*
    var eTmpl = Template.fromXmlFile("/panel.xml");
    println("font:"+font.toString);
    println("texture:"+tex.toString);
    for {
      tmpl <- eTmpl
    } {
      val entity = tmpl.call(js.Dictionary(
        "font" -> font.id,
        "res" -> js.Dictionary(
          "sheet" -> 0,
          "star" -> tex.id
        )
      ))
      println(entity)
    }*/



  }





  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
