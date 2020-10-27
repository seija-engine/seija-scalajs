package s2d

import core.{BaseComponent, Component, Entity, Foreign, TemplateComponent, TemplateParam, World}
import math.Vector2

import scala.scalajs.js

class Transparent(override val entity:Entity) extends BaseComponent(entity)

object Transparent {
  implicit val transparentComp: Component[Transparent] = new Component[Transparent] {
    override def addToEntity(e: Entity): Transparent = {
      Foreign.setTransparent(World.id,e.id,isTransparent = true)
      new Transparent(e)
    }
    
    override val key: Int = 5
  }
}

class TransparentTmpl extends TemplateComponent {
  override val name: String = "Transparent"
  def attachComponent(entity: Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any]):Unit = {
    println("attach Transparent");
    entity.addComponent[Transparent]()
  }
}