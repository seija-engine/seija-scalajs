package demo



import seija.assets
import seija.assets.Loader
import seija.core.event.{CABEventRoot, EventHandle, EventNode, GameEventType}
import seija.core.{Entity, IGame, Template, Transform, TransformTmpl}
import seija.data.Color
import seija.math.Vector3
import seija.s2d.assets.{Font, Image, SpriteSheet}
import seija.ui.Control

class DemoGame extends IGame {


  override def onStart(): Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    Template.setRootPath("../seija-deno/src/tests/res/tmpl")
    seija.ui.Control.setRootPath("../seija-deno/src/tests/res/ui")
    Control.env.put("zeroVec",Vector3.New(0,0,0))

    val font = Loader.loadSync[Font]("WenQuanYiMicroHei.ttf").toOption.get;
    val tex = Loader.loadSync[Image]("StarIcon.png").toOption.get;
    val materialSheet = Loader.loadSync[SpriteSheet]("material.json").toOption.get;

    seija.ui.UISystem.init()
    val imageControl = Control.create("/core/Image.xml","Image")
    imageControl match {
      case Left(value) => println(value)
      case Right(value) => value.Enter()
    }
  }





  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
