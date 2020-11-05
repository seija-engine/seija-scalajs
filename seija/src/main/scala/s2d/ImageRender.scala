package s2d
import core.{BaseComponent, Component, Entity, Foreign, TemplateComponent, TemplateParam, World}
import s2d.assets.Image
import data.Color
import math.Vector2
import data.CoreRead._;

import scala.scalajs.js;
class ImageRender(override val entity:Entity) extends BaseComponent(entity) with GenericImage[ImageRender] {
  

  def setTexture(image:Image):Unit = {
    Foreign.setImageTexture(this.entity.id,image.id)
  }

  override def setImageType(typ: ImageType): Unit = {
    Foreign.setImageType(entity.id,typ.toJsValue)
  }

  override def colorFromRust(): Unit = Foreign.getImageColor(this.entity.id,_color.inner())
  override def colorToRust():Unit = Foreign.setImageColor(this.entity.id,_color.inner())
  override def setFilledValue(v: Float): Unit = Foreign.setImageFilledValue(entity.id,v)
}


object ImageRender {
  implicit val imageRenderComp: Component[ImageRender] = new Component[ImageRender] {
    override val key: String = "ImageRender"
    override def addToEntity(e: Entity): ImageRender = {
      Foreign.addImageRender(e.id,None)
      new ImageRender(e)
    }
  }
}

class ImageRenderTmpl extends TemplateComponent {
  override val name: String = "ImageRender"
  def attachComponent(entity: Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]]):Unit = {
    println("attach ImageRender")
    var imageRender = entity.addComponent[ImageRender]();
    TemplateParam.setValueByAttrDic[Int](attrs,"texture",id => imageRender.setTexture(new Image(id)),data,parentConst)
                 .left.foreach(v => println(s"ImageRender.texture error:$v"))
    TemplateParam.setValueByAttrDic[Color](attrs,"color",imageRender.color = _,data,parentConst)
                 .left.foreach(v => println(s"ImageRender.color error:$v"))
    TemplateParam.setValueByAttrDic[ImageType](attrs,"type",imageRender.setImageType(_),data,parentConst)
                 .left.foreach(v => println(s"ImageRender.type error:$v"))

  }
}