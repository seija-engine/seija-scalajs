package demo
import seija.assets
import seija.assets.Loader
import seija.core.event.{CABEventRoot, EventHandle, EventNode, EventSystem, GameEventType}
import seija.core.{Entity, IGame, Template, Transform, TransformTmpl}
import seija.data.{Color, DynClass, DynObject}
import seija.math.{Vector2, Vector3}
import seija.s2d.Rect2D
import seija.s2d.assets.{Font, Image, SpriteSheet}
import seija.ui2.{Control, UISystem}
import seija.ui2.EventBoard
import seija.ui2.EventBoardComponent

class DemoGame extends IGame {

  override def onStart(): Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    Template.setRootPath("../seija-deno/src/tests/res/tmpl")

    val font = Loader.loadSync[Font]("WenQuanYiMicroHei.ttf").toOption.get;
    val tex = Loader.loadSync[Image]("StarIcon.png").toOption.get;
    println(tex)
    val materialSheet = Loader.loadSync[SpriteSheet]("material.json").toOption.get;
    val paperSheet = Loader.loadSync[SpriteSheet]("paper.json").toOption.get;

    val rootEntity = Entity.New()
    rootEntity.addComponent[Transform]()
    val rect = rootEntity.addComponent[Rect2D]()
    rect.size.set(1024,768)
    rootEntity.addComponent[CABEventRoot]()

    UISystem.initCore()
    UISystem.rootPath = "src/Resource/UI"
    UISystem.env.put("sheet",materialSheet.id)
    UISystem.env.put("paperSheet",paperSheet.id)
    UISystem.env.put("star",tex.id)
    UISystem.env.put("font",font.id)
    val demoModel = new demo.TestModel()
    demoModel.init()
    val panelControl = UISystem.create("/TestPanel.xml",dataContent = Some(demoModel));
    panelControl match {
      case Left(value) => println(value)
      case Right(value) =>
        val eventBoard = EventBoard.boards("TestP")
        eventBoard.addEventRecv(demoModel)
        demoModel.addEventRecv(eventBoard)
        

        value.entity.get.setParent(Some(rootEntity))
    }
  }


  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
