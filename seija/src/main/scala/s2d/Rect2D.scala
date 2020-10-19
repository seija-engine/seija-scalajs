package s2d

import core.{Component, Entity, Foreign, Transform, World}

class Rect2D(private var entity:Entity) {

}


object Rect2D {
  implicit val rect2dComp: Component[Rect2D] = new Component[Rect2D] {
    override def addToEntity(e: Entity): Rect2D = {
      Foreign.addRect2D(World.id,e.id);
      new Rect2D(e)
    }
    override def key(): Int = 1
  }
}