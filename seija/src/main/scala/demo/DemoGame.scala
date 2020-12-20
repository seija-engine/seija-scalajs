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

class DemoGame extends IGame {
  override def onStart(): Unit = {
    TestUI.loadLayout()

  }




  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
