package seija.s2d

import seija.core.{BaseComponent, Component, Entity, Foreign, TemplateComponent}


import scala.scalajs.js

class Transparent(override val entity:Entity) extends BaseComponent(entity)

object Transparent {
  implicit val transparentComp: Component[Transparent] = new Component[Transparent] {
    override def addToEntity(e: Entity): Transparent = {
      Foreign.setTransparent(e.id,isTransparent = true)
      new Transparent(e)
    }
    
    override val key: String = "Transparent"
  }
}

class TransparentTmpl extends TemplateComponent {
  override val name: String = "Transparent"
  def attachComponent(entity: Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]]):Unit = {
    entity.addComponent[Transparent]()
  }
}