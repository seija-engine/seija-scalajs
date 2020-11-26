import demo.DemoGame
import seija.core.App
import seija.data.Color
import seija.s2d.{SWindow, Simple2d}
import seija.data.{SContent, SExprInterp}
import seija.data.{DynClass, DynObject}
import scala.collection.mutable.HashMap

case class User(val realName:String)
class Jvav {
  var user:User = User("aaaaa")
}

object Main {
  def main(args: Array[String]): Unit = {
    SExprInterp.init()
    seija.data.DynObject.init()

    DynObject.registerClass[User]()
    DynObject.registerClass[Jvav]()
    var jvav = new Jvav()
    val ret = DynObject.findValue("user.realName",jvav)
    println(ret)
    /*
    val app = new App(new DemoGame,new Simple2d(new SWindow(
      bgColor = Color.New(0.9f,0.9f,0.9f,1f),
      width = 177,
      height = 144
    )));
    app.run()*/
  }
  
}