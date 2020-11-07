package seija.core.event


import seija.core.{BaseComponent, Component, Entity, Foreign}

class CABEventRoot(override val entity:Entity) extends BaseComponent(entity) {

}

object CABEventRoot {
  implicit val cabEventRootComp: Component[CABEventRoot] = new Component[CABEventRoot] {
    override def addToEntity(e: Entity): CABEventRoot = {
      Foreign.addCABEventRoot(e.id)
      new CABEventRoot(e)
    }
    override val key:String = "CABEventRoot"
  }
}