package core
import scala.collection.mutable
import scala.scalajs.js
class Entity(val id:Int) {
  private var _parent:Option[Entity] = None;
  private var components:mutable.HashMap[Int,Any] = mutable.HashMap()
  private var _childrens:js.Array[Entity] = js.Array()
  override def toString: String = {
    s"Entity($id)"
  }

  def addComponent[T]()(implicit comp:Component[T]):T = {
    val t = comp.addToEntity(this)
    this.components.put(comp.key(),t);
    t
  }

  def getComponent[T]()(implicit comp:Component[T]):T = {
    this.components(comp.key()).asInstanceOf[T]
  }

  def setParent(parent:Entity):Unit = {
    parent.addChildren(this)
  }

  def addChildren(entity: Entity):Unit = {
    entity._parent = Some(entity);
    this._childrens.push(entity);
    Foreign.entitySetParent(World.id,entity.id,this.id)
  }

  def childrens:js.Array[Entity] = this._childrens
}

object Entity {
  def New():Entity = new Entity(Foreign.newEntity(World.id))

}
