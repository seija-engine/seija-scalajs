package demo

import seija.assets
import seija.assets.Loader
import seija.core.event.{CABEventRoot, EventHandle, EventNode, EventSystem, GameEventType}
import seija.core.{Entity, IGame, Template, Transform, TransformTmpl}
import seija.data.{Color, DynClass, DynObject}
import seija.math.{Vector2, Vector3}
import seija.s2d.{ImageRender, Rect2D, Transparent}
import seija.s2d.assets.{Font, Image, SpriteSheet}
import seija.s2d.layout.{GridCell, GridLayout, LRate, LayoutAlignment, LayoutView, Orientation, StackLayout, Thickness}
import seija.ui2.{Control, UISystem}
import seija.ui2.EventBoard
import seija.ui2.EventBoardComponent

import scala.scalajs.js

object TestUI {
  def load():Unit = {


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


  def loadLayout():Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    Template.setRootPath("../seija-deno/src/tests/res/tmpl")
    val colorTex = Loader.loadSync[Image]("white.png").toOption.get

    val root = Entity.New()
    root.addComponent[Transform]()
    root.addComponent[Rect2D]()
    root.addComponent[Transparent]()
    val img = root.addComponent[ImageRender]()
    img.setTexture(colorTex)
    img.color.set(0.1f,0.1f,0.1f,1f)
    val grid = root.addComponent[GridLayout]()
    grid.addRow(LRate(2))
    grid.addRow(LRate(8))
    grid.addCol(LRate(5))

    val g0 = addImg(colorTex,root,Some(Color.mblue) )
    val cell = g0.addComponent[GridCell]()
    cell.setRow(1)

    val stackEntity = addImg(colorTex,root,Some(Color.silver),addView = false)
    val cell2 = stackEntity.addComponent[GridCell]()

    stackEntity.removeComponent[LayoutView]()
    val stack = stackEntity.addComponent[StackLayout]()
    stack.setOrientation(Orientation.Vertical)
    stack.setSpacing(10)


    val menu0 = addImg(colorTex,stackEntity,Some(Color.green))
    val view0 = menu0.getComponent[LayoutView]().get
    view0.setSize(Vector2.New(0,30))
    view0.setHor(LayoutAlignment.Fill)
    view0.setMargin(Thickness(0,5,0,0))

    val menu1 = addImg(colorTex,stackEntity,Some(Color.green))
    val view1 = menu1.getComponent[LayoutView]().get
    view1.setSize(Vector2.New(0,30))
    view1.setHor(LayoutAlignment.Fill)

    val menu2 = addImg(colorTex,stackEntity,Some(Color.green))
    val view2 = menu2.getComponent[LayoutView]().get
    view2.setHor(LayoutAlignment.Fill)
    view2.setSize(Vector2.New(-1,30))
    view2.setMargin(Thickness(0,0,0,0))
  }

  def addImg(tex:Image,parent:Entity,c: Option[Color] = None,addView:Boolean = true):Entity = {
    val img1 = Entity.New()
    img1.addComponent[Transform]()
    img1.addComponent[Transparent]()
    img1.addComponent[Rect2D]()
    if(addView) {
      val view = img1.addComponent[LayoutView]()

    }
    val r = img1.addComponent[ImageRender]()
    r.setTexture(tex)

    r.color = c.getOrElse(Color.New(1,1,1,1))
    img1.setParent(Some(parent))
    img1
  }

  def loadUILayout():Unit = {
    val colorTex = Loader.loadSync[Image]("white.png").toOption.get
    UISystem.env.put("res",js.Dictionary("white"-> colorTex.id))

    val root = Entity.New()
    root.addComponent[Transform]()
    root.addComponent[CABEventRoot]()
    val rect2d = root.addComponent[Rect2D]()
    rect2d.size.set(1024,768)

    val layoutPanel = UISystem.create("/GridLayout.xml")
    layoutPanel match {
      case Left(value) => println(value)
      case Right(value) => //value.entity.get.setParent(Some(root))
    }

  }
}
