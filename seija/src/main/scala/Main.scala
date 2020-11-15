
import demo.DemoGame
import seija.core.App
import seija.data.Color
import seija.s2d.{SWindow, Simple2d}
import seija.data.{SExprInterp,SContent}
import seija.ui.Control
object Main {
  def main(args: Array[String]): Unit = {
    SExprInterp.init()
    Control.init()

    val app = new App(new DemoGame,new Simple2d(new SWindow(
      bgColor = Color.New(0.9f,0.9f,0.9f,1f),
      width = 320,
      height = 240
    )));
    app.run()
  }
  
}