package s2d.assets
import assets.{Asset,IAsset }
import core.{Component, Entity, Foreign, World}
import s2d.Transparent;

class Image(override val id:Int) extends Asset(id){
  type Config = TextureConfig
}

object Image {
  implicit val imageAsset: IAsset[Image] = new  IAsset[Image] {
    override def fromId(id: Int): Image = new Image(id)
    override val assetType: Int = 2
  }
}