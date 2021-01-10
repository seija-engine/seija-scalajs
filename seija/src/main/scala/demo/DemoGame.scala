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

import slogging.{ConsoleLoggerFactory, LazyLogging, LogLevel, LoggerConfig, PrintLoggerFactory, StrictLogging}



class DemoGame extends LazyLogging with IGame {
  override def onStart(): Unit = {
    assets.Loader.setAssetRoot("../seija-deno/src/tests/res/")
    
  


  }




  override def onUpdate(): Unit = {

  }

  override def onQuit(): Unit = {


  }


}
