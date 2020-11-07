package seija.s2d

import scala.scalajs.js
import seija.core.{BaseComponent, Component, Entity, Foreign, TemplateComponent, TemplateParam}
import seija.data.Color
import seija.data.CoreRead._
import seija.s2d.assets.SpriteSheet

class SpriteRender(override val entity:Entity) extends BaseComponent(entity) with GenericImage[SpriteRender] {

  private var _spriteName:String = "";
  def spriteName:String = _spriteName

  def setSpriteSheet(sheet:SpriteSheet):Unit = {
    Foreign.setSpriteSheet(this.entity.id,sheet.id)
  }

  def setSpriteName(name:String):Unit = {
    Foreign.setSpriteName(this.entity.id,name)
    _spriteName = name;
  }

  def setSliceByConfig(idx:Int):Unit =
    Foreign.setSpriteSliceByConfig(entity.id,idx)

  override def colorToRust(): Unit = Foreign.setSpriteColor(entity.id,this._color.inner())
  override def colorFromRust(): Unit = Foreign.getSpriteColor(entity.id,this._color.inner())

  override def setImageType(typ: ImageType): Unit = Foreign.setSpriteType(entity.id,typ.toJsValue)
  override def setFilledValue(v: Float): Unit = Foreign.setSpriteFilledValue(entity.id,v)
}


object SpriteRender {
  implicit val spriteRenderComp: Component[SpriteRender] = new Component[SpriteRender] {
    override val key: String = "SpriteRender"
    override def addToEntity(e: Entity): SpriteRender = {
      Foreign.addSpriteRender(e.id)
      new SpriteRender(e)
    }
  }
}

class SpriteRenderTmpl extends TemplateComponent {
  override val name: String = "SpriteRender"
  def attachComponent(entity: Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]]):Unit = {
      val spriteRender = entity.addComponent[SpriteRender]();
      TemplateParam.setValueByAttrDic[String](attrs,"spriteName",spriteRender.setSpriteName,data,parentConst)
                   .left.foreach(v => println(s"SpriteRender.spriteName error: $v"))
      TemplateParam.setValueByAttrDic[Int](attrs,"sheet",id => spriteRender.setSpriteSheet(new SpriteSheet(id)),data,parentConst)
                   .left.foreach(v => println(s"SpriteRender.sheet error: $v"))
      TemplateParam.setValueByAttrDic[Color](attrs,"color",spriteRender.color = _,data,parentConst)
                   .left.foreach(v => println(attrs,s"SpriteRender.color error: $v"))
      TemplateParam.setValueByAttrDic[ImageType](attrs,"type",spriteRender.setImageType,data,parentConst)
                   .left.foreach(v => println(s"SpriteRender.type error: $v"))
  }
}