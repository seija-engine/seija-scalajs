package sled
import seija.core.IGame
import seija.core.event.CABEventRoot
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView
import slogging.LazyLogging
import seija.ui2.UISystem
import scala.scalajs.js
class SledWindow extends IGame with LazyLogging {
  
  override def onStart()  {
    Assets.init()
    UISystem.ENV.put("res",js.Dictionary(
      "sheet" -> Assets.chromeSheet.get.id,
      "white" -> Assets.white.get.id,
      "font" -> Assets.font.get.id
    ))
    
    
    UISystem.initCore("./res/UI/")
    UISystem.create("MainWindow.xml") match {
      case Left(value) => logger.error(value)
      case Right(control) =>

    }
  }

  

  override def onUpdate() {

  }

  override def onQuit()  {

  }
}
