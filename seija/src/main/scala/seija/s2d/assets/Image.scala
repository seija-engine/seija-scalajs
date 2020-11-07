package seija.s2d.assets

import seija.assets.{Asset, IAsset}
import seija.s2d.Transparent;

class Image(override val id:Int) extends Asset(id){
  type Config = TextureConfig

  override def toString: String = s"Image($id)"
}

object Image {
  implicit val imageAsset: IAsset[Image] = new  IAsset[Image] {
    override def fromId(id: Int): Image = new Image(id)
    override val assetType: Int = 1
  }
}