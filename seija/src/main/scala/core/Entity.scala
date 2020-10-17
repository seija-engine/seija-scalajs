package core
import scala.collection.mutable
import scala.scalajs.js
class Entity(val id:Int) {
  private var components:mutable.HashMap[Int,Component[Any]] = mutable.HashMap()
  override def toString: String = {
    s"Entity($id)"
  }

  def addComponent[T]()(implicit comp:Component[T]):T = {
    val t = comp.addToEntity(this)
    this.components.put(comp.key(),t.asInstanceOf[Component[Any]]);
    t
  }

  def getComponent[T]()(implicit comp:Component[T]):T = {
    this.components(comp.key()).asInstanceOf[T]
  }
}

object Entity {
  def New():Entity = new Entity(Foreign.newEntity(World.id))

}
