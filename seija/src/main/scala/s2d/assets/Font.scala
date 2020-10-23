package s2d.assets

import assets.{Asset, IAsset, NullConfig}

class Font(override val id:Int) extends Asset(id) {
  type Config = NullConfig
  override def toString: String = s"Font($id)"
}
object Font {
  implicit val fontAsset: IAsset[Font] = new  IAsset[Font] {
    override def fromId(id: Int): Font = new Font(id)
    override val assetType: Int = 3
  }
}