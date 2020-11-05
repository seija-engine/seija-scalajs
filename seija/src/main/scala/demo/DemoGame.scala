package demo
import math.Vector3
import core.{App, Entity, IGame, Template, Time, Transform, TransformTmpl}
import s2d.{ImageFilled, ImageRender, ImageRenderTmpl, Rect2D, Rect2DTmpl, SpriteRender, SpriteRenderTmpl, TextRender, TextRenderTmpl, Transparent, TransparentTmpl}
import assets.Loader
import core.event.{CABEventRoot, EventHandle, EventNode, EventSystem, GameEventType}
import core.event.CABEventRoot._
import data.Color
import s2d.assets.{Font, Image, SpriteSheet, TextureConfig}
import data.XmlExt._

import scala.scalajs.js;

class DemoGame extends IGame {

  var evNode:EventNode = null;
  var handle:EventHandle = null;
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
    root.addComponent[CABEventRoot]()
    root.addComponent[Transform]();
    val event = root.addComponent[EventNode]();
    var rect = root.addComponent[Rect2D]()
    rect.size.set(100,100)
    handle = event.register(GameEventType.Click, isCapture = true,this.onClick)
    evNode = event;
    root.info.name = "Root"
    println(root.info.name)

    val e2 = Entity.New()
    e2.setParent(Some(root))
    e2.addComponent[CABEventRoot]()
    e2.addComponent[Transform]();
    val event2 = e2.addComponent[EventNode]();
    var rect2 = e2.addComponent[Rect2D]()
    rect2.size.set(100,100)
    event2.register(GameEventType.Click,true,() => {
      println("click e2");
    })
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
        } */



  }


  def onClick():Unit = {
    println("OCCCCCC");
    this.evNode.entity.destory()
  }


  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
