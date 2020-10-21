package math
import scala.scalajs.js.typedarray.Float32Array;
import scala.scalajs.js;
class Vector2(private var _inner:Float32Array) {
  def inner():Float32Array = this._inner

  def x:Float = this._inner.get(0)
  def y:Float = this._inner.get(1)

  def x_= (x:Float): Unit = {
    this._inner.set(0,x)
    this.callCallBack()
  }

  def y_= (y:Float): Unit = {
    this._inner.set(1,y)
    this.callCallBack()
  }

  def set(x:Float,y:Float):Unit = {
    this._inner.set(0,x);
    this._inner.set(1,y)

    this.callCallBack()
  }


  var updateCallback:Option[() => Unit] = None;
  def callCallBack():Unit = if(this.updateCallback.isDefined) { this.updateCallback.get(); }
  def setCallBack(f:() => Unit):Unit = this.updateCallback = Some(f)

  override def toString = s"Vector2(${this.x},${this.y})"
}


object Vector2 {
  def New(x:Float,y:Float):Vector2 = new Vector2(Float32Array.from(js.Array(x,y)))

  def default():Vector2 = new Vector2(Float32Array.from(js.Array(0,0)))

  def defaultByCB(f:() => Unit,x:Float = 0,y:Float = 0): Vector2 = {
    var vec = Vector2.New(x,y);
    vec.setCallBack(f);
    vec
  }
}