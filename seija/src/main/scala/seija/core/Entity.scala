package seija.core

import seija.core.event.EventSystem

import scala.collection.mutable
import scala.scalajs.js

class Entity(val id:Int) {
  private var _parent:Option[Entity] = None;
  private var components:mutable.HashMap[String,BaseComponent] = mutable.HashMap()
  private var _childrens:js.Array[Entity] = js.Array()
  private var _info:Option[EntityInfo] = None

  override def toString: String = s"Entity($id)"

  def info:EntityInfo = {
    if(this._info.isEmpty) {
      Foreign.addEntityInfo(id,"")
      _info = Some(new EntityInfo(this))
    }
    this._info.get
  }

  def parent:Option[Entity] = this._parent


  def isAlive:Boolean = Foreign.entityIsAlive(this.id)

  def addComponent[T <: BaseComponent]()(implicit comp:Component[T]):T = {
    val t = comp.addToEntity(this)
    t.onAttach()
    this.components.put(comp.key,t);
    t
  }

  def getComponent[T <: BaseComponent]()(implicit comp:Component[T]):Option[T] = {
    this.components.get(comp.key).map(_.asInstanceOf[T])
  }

  def removeComponent[T <: BaseComponent]()(implicit comp:Component[T]): Unit = {
    this.components.remove(comp.key).foreach(_.onDetach())
  }

  def setParent(parent:Option[Entity]):Unit = {
    if(this._parent != parent) {
      this.removeFromParent();
      if(parent.isDefined) {
        this._parent = Some(parent.get)
        parent.get._childrens.push(this)
        Foreign.entitySetParent(this.id,parent.get.id);
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
    if(this._parent.isEmpty) {
      EventSystem.unRegEventNode(this.id)
    }
    this.removeFromParent()
    Foreign.deleteEntity(this.id)
  }
}

object Entity {
  def New():Entity = new Entity(Foreign.newEntity)

  def all():js.Array[Int] = {
    Foreign.entityAll
  }
}