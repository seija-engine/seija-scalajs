package demo
import seija.assets
import seija.assets.Loader
import seija.core.event.{CABEventRoot, EventHandle, EventNode, EventSystem, GameEventType}
import seija.core.{Entity, IGame, Template, Transform, TransformTmpl}
import seija.data.{Color, DynClass, DynObject}
import seija.math.{Vector2, Vector3}
import seija.s2d.Rect2D
import seija.s2d.assets.{Font, Image, SpriteSheet}
import seija.s2d.layout.{LayoutAlignment, LayoutView, Thickness}
import seija.ui2.{Control, UISystem}
import seija.ui2.EventBoard
import seija.ui2.EventBoardComponent

class DemoGame extends IGame {
  var root:Option[Entity] = None
  override def onStart(): Unit = {
    val root = Entity.New()
    val view = root.addComponent[LayoutView]()
    root.addComponent[Transform]()
    root.addComponent[Rect2D]()
    this.root = Some(root)

    view.setMargin(new Thickness(10))
    view.setSize(Vector2.New(100,0))
    view.setHor(LayoutAlignment.Start)



  }


  override def onUpdate(): Unit = {
    if(this.root.isDefined) {
      val size = this.root.get.getComponent[Rect2D]().get.size
      println(size.toString)
    }
  }

  override def onQuit(): Unit = {


  }


}
