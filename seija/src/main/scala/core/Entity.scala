package core

class Entity(val id:Int) {
  override def toString: String = {
    s"Entity($id)"
  }

  def addComponent[T <: BaseComponent](): Unit = {

  }
}

object Entity {
  def New():Entity = new Entity(Foreign.newEntity(World.id))

}
