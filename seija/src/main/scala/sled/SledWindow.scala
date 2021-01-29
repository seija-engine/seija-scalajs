package sled
import seija.core.IGame
import seija.core.event.CABEventRoot
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView
import slogging.LazyLogging
import seija.ui.UISystem
import scala.scalajs.js
import seija.ui.ControlParams
import seija.core.Entity
import seija.core.Transform
import seija.core.event.EventNode
import seija.s2d.layout.LayoutView
import seija.s2d.layout.LayoutAlignment
import seija.math.Vector3
class SledWindow extends IGame with LazyLogging {
  
  override def onStart()  {
    Assets.init()

    UISystem.ENV.put("res",js.Dictionary(
      "sheet" -> Assets.chromeSheet.get.id,
      "white" -> Assets.white.get.id,
      "font" -> Assets.font.get.id
    ))

    UISystem.init("./res/UI/",js.Array("Normal","NormalMenu","Dialog","DialogMenu"))

    UISystem.createByFile("MainWindow.xml",None, ControlParams(),None) match {
      case Left(errString) => logger.error(errString)
      case Right(control) => 
         
    }
    logger.info(seija.os.pwd.toString())
    UISystem.Update()
  }

  

  override def onUpdate() {
    UISystem.Update()
  }

  override def onQuit()  {

  }
}
