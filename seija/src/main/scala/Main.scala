import core.Foreign

import scalajs.js
import core.App
import data.Color
import s2d.{SWindow, Simple2d}
import demo.DemoGame
object Main {
  def main(args: Array[String]): Unit = {
    val app = new App(new DemoGame,new Simple2d(new SWindow(
      bgColor = Color.New(0f,0f,0f,1f),
      width = 1024,
      height = 768
    )));
    app.run()

  }
}