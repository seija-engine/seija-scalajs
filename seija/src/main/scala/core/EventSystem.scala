package core
import scala.scalajs.js;
object EventSystem {
  var globalCallbacks:js.Array[() => Unit] = js.Array()
  def handleEvent(event:js.Array[js.Any]):Unit = {
    if(event(0).asInstanceOf[Int] == 0) {
      println(this.globalCallbacks.length)
      this.globalCallbacks.foreach(f => f())
    } else {
      
    }
  }

  def regKeyBoard(f:() => Unit):Int = {
    this.globalCallbacks.push(f)
  }

  def removeKeyBoard(f:() => Unit):Unit = {
    var idx = 0;
    for(cb <- this.globalCallbacks) {
       if(cb == f) {
         this.globalCallbacks.remove(idx)
         return;
       }
      idx +=1;
     }
  }

}
