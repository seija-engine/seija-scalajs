package assets
import scala.scalajs.js;
trait ToJSValue {
  def toJsValue():js.Any
}

abstract class Asset(val id:Int) {
  type Config <: ToJSValue
}

trait IAsset[T] {
  def fromId(id:Int):T
  val assetType:Int
}