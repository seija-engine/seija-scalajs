package s2d
import core.{BaseComponent, Component, Entity, Foreign, World, TemplateComponent,TemplateParam}
import data.Color
import s2d.assets.SpriteSheet
import scala.scalajs.js
import data.CoreRead._

class SpriteRender(override val entity:Entity) extends BaseComponent(entity) with GenericImage[SpriteRender] {

  var _spriteName:String = "";
  def spriteName:String = _spriteName

  def setSpriteSheet(sheet:SpriteSheet):Unit = {
    Foreign.setSpriteSheet(World.id,this.entity.id,sheet.id)
  }

  def setSpriteName(name:String):Unit = {
    Foreign.setSpriteName(World.id,this.entity.id,name)
    _spriteName = name;
  }

  def setSliceByConfig(idx:Int):Unit =
    Foreign.setSpriteSliceByConfig(World.id,entity.id,idx)

  override def colorToRust(): Unit = Foreign.setSpriteColor(World.id,entity.id,this._color.inner())
  override def colorFromRust(): Unit = Foreign.getSpriteColor(World.id,entity.id,this._color.inner())

  override def setImageType(typ: ImageType): Unit = Foreign.setSpriteType(World.id,entity.id,typ.toJsValue)
  override def setFilledValue(v: Float): Unit = Foreign.setSpriteFilledValue(World.id,entity.id,v)
}


object SpriteRender {
  implicit val spriteRenderComp: Component[SpriteRender] = new Component[SpriteRender] {
    override val key: Int = 3
    override def addToEntity(e: Entity): SpriteRender = {
      Foreign.addSpriteRender(World.id,e.id)
      new SpriteRender(e)
    }
  }
}

class SpriteRenderTmpl extends TemplateComponent {
  override val name: String = "SpriteRender"
  def attachComponent(entity: Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any]):Unit = {
      println("attach SpriteRender")
      var spriteRender = entity.addComponent[SpriteRender]();
      TemplateParam.setValueByAttrDic[String](attrs,"spriteName",spriteRender.setSpriteName(_),data)
                   .left.foreach(v => println(s"SpriteRender.spriteName error: $v"))
      TemplateParam.setValueByAttrDic[Int](attrs,"sheet",id => spriteRender.setSpriteSheet(new SpriteSheet(id)),data)
                   .left.foreach(v => println(s"SpriteRender.sheet error: $v"))
      TemplateParam.setValueByAttrDic[Color](attrs,"color",spriteRender.color = _,data)
                   .left.foreach(v => println(attrs,s"SpriteRender.color error: $v"))
      TemplateParam.setValueByAttrDic[ImageType](attrs,"type",spriteRender.setImageType(_),data)
                   .left.foreach(v => println(s"SpriteRender.type error: $v"))
  }
}