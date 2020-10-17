import core.Foreign
import scalajs.js;
import core.App
import s2d.Simple2d
import demo.DemoGame
object Main {
  def main(args: Array[String]): Unit = {
    val app = new App(new DemoGame,new Simple2d);
    app.run()
  }
}