package s2d

import core.{BaseComponent, Component, Entity, Foreign, World}

class Transparent(override val entity:Entity) extends BaseComponent(entity)

object Transparent {
  implicit val transparentComp: Component[Transparent] = new Component[Transparent] {
    override def addToEntity(e: Entity): Transparent = {
      Foreign.setTransparent(World.id,e.id,isTransparent = true)
      new Transparent(e)
    }
    
    override val key: Int = 2
  }
}