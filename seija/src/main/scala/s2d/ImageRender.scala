package s2d
import core.{BaseComponent, Component, Entity, Foreign, World}
import s2d.assets.Image;
import data.Color;
class ImageRender(override val entity:Entity) extends BaseComponent(entity) with GenericImage[ImageRender] {
  

  def setTexture(image:Image):Unit = {
    Foreign.setImageTexture(World.id,this.entity.id,image.id)
  }

  override def setImageType(typ: ImageType): Unit = {
    println(typ.toJsValue)
    Foreign.setImageType(World.id,entity.id,typ.toJsValue)
  }

  override def colorFromRust(): Unit = Foreign.getImageColor(World.id,this.entity.id,_color.inner())
  override def colorToRust():Unit = Foreign.setImageColor(World.id,this.entity.id,_color.inner())
  override def setFilledValue(v: Float): Unit = Foreign.setImageFilledValue(World.id,entity.id,v)
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