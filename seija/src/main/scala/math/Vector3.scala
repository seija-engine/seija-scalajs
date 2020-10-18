package math
import scala.scalajs.js.typedarray.Float32Array;
import scala.scalajs.js;
class Vector3(private var _inner:Float32Array) {
  def x:Float = this._inner.get(0)
  def y:Float = this._inner.get(1)
  def z:Float = this._inner.get(2)

  def x_= (x:Float): Unit = {
    this._inner.set(0,x)
    this.callCallBack()
  }

  def y_= (y:Float): Unit = {
    this._inner.set(1,y)
    this.callCallBack()
  }

  def z_= (z:Float): Unit = {
    this._inner.set(2,z)
    this.callCallBack()
  }


  def inner():Float32Array = this._inner

  var updateCallback:Option[() => Unit] = None;
  def callCallBack():Unit = {
    if(this.updateCallback.isDefined) {
      this.updateCallback.get();
    }
  }

  def setCallBack(f:() => Unit):Unit = this.updateCallback = Some(f)

  override def toString: String = {
    s"Vector3(${this.x},${this.y},${this.z})"
  }

}

object Vector3 {
  def default():Vector3 = {
    var arr = Float32Array.from(js.Array(0,0,0));
    new Vector3(arr)
  }

  def defaultByCB(f:() => Unit): Vector3 = {
    var vec = Vector3.default();
    vec.setCallBack(f);
    vec
  }

  def New(x:Float,y:Float,z:Float) :Vector3 = {
    new Vector3(Float32Array.from(js.Array(x,y,z)))
  }
}
