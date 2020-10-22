package s2d
import core.{BaseComponent, Component, Entity, Foreign, World}
import data.Color
import s2d.assets.SpriteSheet

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



  override def colorToRust(): Unit = Foreign.setSpriteColor(World.id,entity.id,this._color.inner())
  override def colorFromRust(): Unit = Foreign.getSpriteColor(World.id,entity.id,this._color.inner())
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