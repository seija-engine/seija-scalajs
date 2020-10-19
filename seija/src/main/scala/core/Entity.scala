package core
import scala.collection.mutable
import scala.reflect.ClassTag
import scala.scalajs.js

class Entity(val id:Int) {
  private var _parent:Option[Entity] = None;
  private var components:mutable.HashMap[Int,Any] = mutable.HashMap()
  private var _childrens:js.Array[Entity] = js.Array()

  override def toString: String = s"Entity($id)"


  def isAlive:Boolean = Foreign.entityIsAlive(World.id,this.id)
  

  def addComponent[T]()(implicit comp:Component[T]):T = {
    val t = comp.addToEntity(this)

    this.components.put(comp.key(),t);
    t
  }

  def getComponent[T]()(implicit comp:Component[T]):T = {
    this.components(comp.key()).asInstanceOf[T]
  }

  def setParent(parent:Option[Entity]):Unit = {
    if(this._parent != parent) {
      this.removeFromParent();
      if(parent.isDefined) {
        this._parent = Some(parent.get)
        parent.get._childrens.push(this)
        Foreign.entitySetParent(World.id,this.id,parent.get.id);
      }
    }
  }

  def childrens:js.Array[Entity] = this._childrens

  private def removeFromParent():Unit = {
      if(this._parent.isDefined) {
        var index = this._parent.get._childrens.indexOf(this);
        this._parent.get._childrens.remove(index);
      }
  }

  def destory():Unit = {
    this.removeFromParent();
    Foreign.deleteEntity(World.id,this.id)
  }
}

object Entity {
  def New():Entity = new Entity(Foreign.newEntity(World.id))

  def all():js.Array[Int] = {
    Foreign.entityAll(World.id)
  }
}
