package s2d
import core.{BaseComponent, Component, Entity, Foreign, World}
import s2d.assets.Image;
class ImageRender(override val entity:Entity) extends BaseComponent(entity) {
  def setTexture(image:Image):Unit = {
    Foreign.setImageTexture(World.id,this.entity.id,image.id)
  }
}


object ImageRender {
  implicit val imageRenderComp: Component[ImageRender] = new Component[ImageRender] {
    override val key: Int = 3
    override def addToEntity(e: Entity): ImageRender = {
      Foreign.addImageRender(World.id,e.id,None)
      new ImageRender(e)
    }

  }
}