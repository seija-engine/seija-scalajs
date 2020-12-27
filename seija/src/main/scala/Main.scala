import demo.DemoGame
import seija.core.App
import seija.data.Color
import seija.s2d.{SWindow, Simple2d}
import seija.data.SExprInterp
import sled.SledWindow
import slogging._


object Main {
  def main(args: Array[String]): Unit = {
    SExprInterp.init()
    seija.data.DynObject.init()
    LoggerConfig.factory = PrintLoggerFactory()
    LoggerConfig.level = LogLevel.TRACE

    val app = new App(new SledWindow,new Simple2d(new SWindow(
      bgColor = Color.New(1f,1f,1f,1f),
      width = 1024,
      height = 768
    )));
    app.run()
  }
}