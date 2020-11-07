package seija.assets

import seija.core.ToJSValue

import scala.scalajs.js;


class NullConfig extends ToJSValue {
  override def toJsValue: js.Any = 0
}

abstract class Asset(val id:Int) {
  type Config <: ToJSValue
}

trait IAsset[T] {
  def fromId(id:Int):T
  val assetType:Int
}