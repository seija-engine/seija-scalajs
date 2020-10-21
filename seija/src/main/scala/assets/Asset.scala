package assets
import core.ToJSValue

import scala.scalajs.js;


abstract class Asset(val id:Int) {
  type Config <: ToJSValue
}

trait IAsset[T] {
  def fromId(id:Int):T
  val assetType:Int
}