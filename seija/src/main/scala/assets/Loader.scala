package assets
import core.{Component, Foreign, World}

import scala.scalajs.js

object Loader {
  def loadSync[T <:Asset](path:String,config:Option[T#Config] = None)(implicit asset:IAsset[T]):Either[String,T] = {
    var mayResId = Foreign.loadSync(World.id,path,asset.assetType,config.map(_.toJsValue).getOrElse(0))
    if(js.typeOf(mayResId) == "string") {
      Left(mayResId.asInstanceOf[String])
    } else {
     Right(asset.fromId(mayResId.asInstanceOf[Int]))
    }
  }

  def setAssetRoot(path:String):Unit = {
    Foreign.setAssetRootPath(World.id,path)
  }
}
