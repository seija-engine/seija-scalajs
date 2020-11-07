package demo

import seija.assets.Loader
import seija.core.event.{CABEventRoot, EventHandle, EventNode, GameEventType}
import seija.core.{Entity, Template, Transform, TransformTmpl}
import seija.s2d.{ImageRenderTmpl, Rect2D, Rect2DTmpl, SpriteRenderTmpl, TextRenderTmpl, TransparentTmpl}
import seija.s2d.assets.{Font, Image}

import scala.scalajs.js

object Tests {
  def loadPanelTemplate():Unit = {
    this.registerAllTemplate()
    val font = Loader.loadSync[Font]("WenQuanYiMicroHei.ttf").toOption.get;
    val tex = Loader.loadSync[Image]("StarIcon.png").toOption.get;
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
      val ref = entity._2;
      println(ref.find("center.img1.img2"))
    }
  }

  def registerAllTemplate():Unit = {
    Template.registerComponent(new TransformTmpl)
    Template.registerComponent(new Rect2DTmpl)
    Template.registerComponent(new TransparentTmpl)
    Template.registerComponent(new TextRenderTmpl)
    Template.registerComponent(new ImageRenderTmpl)
    Template.registerComponent(new SpriteRenderTmpl)
  }

  var evNode:EventNode = null;
  var handle:EventHandle = null;
  def testEvent():Unit = {
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
    event2.register(GameEventType.Click,isCapture = true, () => {
      println("click e2");
    })
  }

  def onClick():Unit = {
    println("OCCCCCC");
    this.evNode.entity.destory()
  }

}
