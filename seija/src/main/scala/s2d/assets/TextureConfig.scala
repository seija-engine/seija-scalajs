package s2d.assets
import assets.ToJSValue;
import scala.scalajs.js;

object Filter extends Enumeration {
    type Filter = Value
    val Nearest:Filter = Value(0)
    val Linear:Filter = Value(1)
}


object WrapMode extends Enumeration {
    type WrapMode = Value;
     val Tile:WrapMode = Value(0)
     val Mirror:WrapMode = Value(1)
     val Clamp:WrapMode = Value(3)
     val Border:WrapMode = Value(4)
}

class SamplerDesc(filter:Filter.Filter,wrapMode:WrapMode.WrapMode)

class TextureConfig(var samplerDesc:Option[SamplerDesc] = None,
                    var generateMips:Option[Boolean] = None,
                    var premultiplyAlpha:Option[Boolean] = None) extends ToJSValue {
    override def toJsValue(): js.Any = {
        
        1233.asInstanceOf[js.Any]
    }

}
