package seija.math

import seija.data.Read

import scala.scalajs.js.typedarray.Float32Array
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

  def set(x:Float,y:Float,z:Float):Unit = {
    this._inner.set(0,x);
    this._inner.set(1,y);
    this._inner.set(2,z);
    this.callCallBack()
  }

  def inner():Float32Array = this._inner

  var updateCallback:Option[() => Unit] = None;
  def callCallBack():Unit = this.updateCallback.foreach(_())


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

  def zero:Vector3 = Vector3.New(0,0,0)
  def one:Vector3 = Vector3.New(1,1,1)

  def New(x:Float,y:Float,z:Float) :Vector3 = {
    new Vector3(Float32Array.from(js.Array(x,y,z)))
  }

  implicit val vector3Read: Read[Vector3] = (string: String) => {
    val splitArr = string.split(',')
    if(splitArr.length == 3) {
      Some(Vector3.New(splitArr(0).toFloat,splitArr(1).toFloat,splitArr(2).toFloat))
    } else {
      None
    }
  }
}
