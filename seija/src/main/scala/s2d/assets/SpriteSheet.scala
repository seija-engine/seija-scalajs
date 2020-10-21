package s2d.assets

import assets.{Asset, IAsset}

class SpriteSheet(override val id:Int) extends Asset(id) {
  type Config = TextureConfig
  override def toString: String = s"SpriteSheet($id)"
}

object SpriteSheet {
  implicit val imageAsset: IAsset[SpriteSheet] = new  IAsset[SpriteSheet] {
    override def fromId(id: Int): SpriteSheet = new SpriteSheet(id)
    override val assetType: Int = 2
  }
}