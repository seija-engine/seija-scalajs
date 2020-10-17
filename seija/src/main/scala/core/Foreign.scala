package core
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object Foreign {
  def init_deno(): Unit = {
    console.log = (v:js.Any) => {
      Deno.core.print(v.toString +"\n")
    }
    Deno.core.ops();
  }

  def newSimple2d(dict:js.Dictionary[js.Any]):Int = Deno.core.jsonOpSync("newSimple2d",dict).asInstanceOf[Int]

  def getAbsoluteTime(w:Int):Float = Deno.core.jsonOpSync("getAbsoluteTime",w).asInstanceOf[Float]

  def closeApp(w:Int):Unit = Deno.core.jsonOpSync("closeApp",w)

  def newEntity(w:Int):Int = Deno.core.jsonOpSync("newEntity",w).asInstanceOf[Int]
}

@js.native
@JSGlobal
object console extends js.Object {
  var log:js.Any = js.native;
}

@js.native
@JSGlobal
object Deno extends js.Object {
  val core:DenoCore = js.native;
}

@js.native
trait DenoCore extends js.Object {
  def print(v:js.Any):Unit
  def ops():Unit
  def jsonOpSync(name:String,value:js.Any):js.Any
}

@js.native
@JSGlobal
object Seija extends js.Object {
  def runApp(int: Int,start:js.Function1[Int,Unit],update:js.Function0[Unit],quit:js.Function0[Unit]):Unit = js.native
}

