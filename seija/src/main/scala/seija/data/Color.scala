package seija.data

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

    override def toString: String = s"Color($r,$g,$b,$a)"

    def toJsArray:js.Array[Float] = js.Array(this.r,this.g,this.b,this.a)
}

object Color {
    def red:Color = Color.New(1,0,0,1)
    def green:Color = Color.New(0,1,0,1)
    def blue:Color = Color.New(0,0,1,1)
    def transparent:Color = Color.New(0,0,0,0)
    def black:Color = Color.New(0,0,0,1)
    def white:Color = Color.New(1,1,1,1)

    def mblue:Color = Color.NewInt(27,161,226,255)
    def bron:Color = Color.NewInt(160,80,0,255)
    def silver:Color = Color.NewInt(192,192,192,255)


    def New(r:Float,g:Float,b:Float,a:Float,callback:Option[() => Unit] = None):Color = {
       var color = new Color(Float32Array.from(js.Array(r,g,b,a)))
       color.callBack = callback
       color
    }

   def NewInt(r:Int,g:Int,b:Int,a:Int,callback:Option[() => Unit] =None):Color = {
     Color.New(r/255f,r/255f,b/255f,a/255f,callback)
   }

   def NewCB(r:Float,g:Float,b:Float,a:Float,f:() => Unit):Color = {
       var color = new Color(Float32Array.from(js.Array(r,g,b,a)))
       color.setCallback(f)
       color
    }

    implicit val colorRead: Read[Color] = (string: String) => {
      val trimString = string.trim;
        if(trimString(0) == '#' && (trimString.length == 7 || trimString.length == 9)) {
            val r = Integer.parseInt(trimString.slice(1,3),16)
            val g = Integer.parseInt(trimString.slice(3,5),16)
            val b = Integer.parseInt(trimString.slice(5,7),16)
            val a = if(trimString.length == 9) {
              Integer.parseInt(trimString.slice(7,9),16)
            } else {
              255
            }
            Some(Color.New(r / 255f, g / 255f,b / 255f,a / 255f))
        } else {
            None
        }
    }
}