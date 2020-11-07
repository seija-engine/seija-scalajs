package demo



import seija.assets
import seija.assets.Loader
import seija.core.event.{CABEventRoot, EventHandle, EventNode, GameEventType}
import seija.core.{Entity, IGame, Template, Transform, TransformTmpl}
import seija.data.Color
import seija.math.Vector3
import seija.s2d.assets.{Font, Image, SpriteSheet}
import seija.s2d.{ImageRenderTmpl, Rect2D, Rect2DTmpl, SpriteRenderTmpl, TextRenderTmpl, TransparentTmpl}
import seija.ui.CheckBox

import scala.scalajs.js;

class DemoGame extends IGame {


  override def onStart(): Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    Template.setRootPath("../seija-deno/src/tests/res/tmpl")
    Tests.registerAllTemplate()
    val font = Loader.loadSync[Font]("WenQuanYiMicroHei.ttf").toOption.get;
    val tex = Loader.loadSync[Image]("StarIcon.png").toOption.get;
    val materialSheet = Loader.loadSync[SpriteSheet]("material.json").toOption.get;
    Template.env.put("res",js.Dictionary("sheet" -> materialSheet.id));

    val root = Entity.New();
    root.addComponent[CABEventRoot]();
    root.addComponent[Transform]();
    val rect2D = root.addComponent[Rect2D]();
    rect2D.size.set(320,240)
    val checkBox = new CheckBox();
    checkBox.onStart(root)
  }





  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
