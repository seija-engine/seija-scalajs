package demo



import seija.assets
import seija.assets.Loader
import seija.core.event.{CABEventRoot, EventHandle, EventNode, GameEventType}
import seija.core.{Entity, IGame, Template, Transform, TransformTmpl}
import seija.data.Color
import seija.math.Vector3
import seija.s2d.assets.{Font, Image, SpriteSheet}
import seija.ui2.UISystem

class DemoGame extends IGame {


  override def onStart(): Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    Template.setRootPath("../seija-deno/src/tests/res/tmpl")

    val font = Loader.loadSync[Font]("WenQuanYiMicroHei.ttf").toOption.get;
    val tex = Loader.loadSync[Image]("StarIcon.png").toOption.get;
    println(tex)
    val materialSheet = Loader.loadSync[SpriteSheet]("material.json").toOption.get;

    UISystem.initCore()
    UISystem.rootPath = "../seija-deno/src/tests/res/ui"
    UISystem.create("/core/Image.xml").foreach(_.Enter())
  }





  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
