package data
import scala.scalajs.js.typedarray.Float32Array;
import scala.scalajs.js;

class Color(private var _inner:Float32Array) {
    
    def r:Float = _inner.get(0)
    def g:Float = _inner.get(1)
    def b:Float = _inner.get(2)
    def a:Float = _inner.get(3)

    def r_= (v:Float):Unit = {
        this._inner.set(0,v)
        this.callCallBack()
    }

    def g_= (v:Float):Unit = {
        this._inner.set(1,v)
        this.callCallBack()
    }

    def b_= (v:Float):Unit = {
        this._inner.set(2,v)
        this.callCallBack()
    }

    def a_= (v:Float):Unit = {
        this._inner.set(3,v)
        this.callCallBack()
    }

    def set(r:Float,g:Float,b:Float,a:Float):Unit = {
        this._inner.set(0,r)
        this._inner.set(1,g)
        this._inner.set(2,b)
        this._inner.set(3,a)
        this.callCallBack()
    }

    def inner():Float32Array = this._inner

    var callBack:Option[()=> Unit] = None
    def setCallback(f:() => Unit):Unit = this.callBack = Some(f)
    def callCallBack():Unit = this.callBack.foreach(_());
}

object Color {
    def New(r:Float,g:Float,b:Float,a:Float,callback:Option[() => Unit] = None):Color = {
       var color = new Color(Float32Array.from(js.Array(r,g,b,a)))
       color.callBack = callback
       color
    }

   def NewCB(r:Float,g:Float,b:Float,a:Float,f:() => Unit):Color = {
       var color = new Color(Float32Array.from(js.Array(r,g,b,a)))
       color.setCallback(f)
       color
    }
}