
import demo.DemoGame
import seija.core.App
import seija.data.Color
import seija.s2d.{SWindow, Simple2d}
import seija.data.{SExprInterp,SContent}
object Main {
  def main(args: Array[String]): Unit = {
    SExprInterp.init()
    val app = new App(new DemoGame,new Simple2d(new SWindow(
      bgColor = Color.New(0.9f,0.9f,0.9f,1f),
      width = 1024,
      height = 768
    )));
    app.run()
  }
  
}