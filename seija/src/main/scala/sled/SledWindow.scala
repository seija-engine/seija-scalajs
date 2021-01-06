package sled
import seija.core.IGame
import seija.core.event.CABEventRoot
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView
import slogging.LazyLogging
import seija.ui.UISystem
import scala.scalajs.js
import seija.ui.ControlParams
class SledWindow extends IGame with LazyLogging {
  
  override def onStart()  {
    Assets.init()

    UISystem.ENV.put("res",js.Dictionary(
      "sheet" -> Assets.chromeSheet.get.id,
      "white" -> Assets.white.get.id,
      "font" -> Assets.font.get.id
    ))

    UISystem.init("./res/UI/")
    UISystem.createByFile("NewPanel.xml",None, ControlParams(),None)
   
    
    
    
    //UISystem.initCore("./res/UI/")
    //UISystem.createByFile("Rewrite.xml",None,ControlParams()) match {
    //  case Left(value) => logger.error(value)
    //  case Right(control) =>
    //}
  }

  

  override def onUpdate() {

  }

  override def onQuit()  {

  }
}
