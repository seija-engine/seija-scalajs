package seija.core.event

import GameEventType._

import scala.collection.mutable
import scala.scalajs.js

import seija.core.{BaseComponent, Component, Entity, Foreign}

class EventNode(override val entity:Entity) extends BaseComponent(entity) {
  protected var captureEvents:mutable.HashMap[Int,js.Array[EventHandle]] = mutable.HashMap();
  protected var bubbleEvents:mutable.HashMap[Int,js.Array[EventHandle]] = mutable.HashMap();

  def setThrough(b:Boolean) {
    Foreign.setEventThrough(this.entity.id,b)
  }

  def register(evType:GameEventType,isCapture:Boolean,callFn:() => Unit):EventHandle = {
    
    Foreign.regEventNodeEvent(entity.id,evType.id,isCapture)
    var dic = if(isCapture) captureEvents else bubbleEvents;
    if(!dic.contains(evType.id)) {
      dic.put(evType.id,js.Array())
    }
    val lst = dic(evType.id);
    val evHandle = new EventHandle(callFn,lst.length)
    dic(evType.id).push(evHandle)
    evHandle
  }

  def unRegister(evType:GameEventType,isCapture:Boolean,handle:EventHandle = null):Unit = {
    var dic = if(isCapture) captureEvents else bubbleEvents;
    if(!dic.contains(evType.id)) return
    var lst = dic(evType.id);
    if(handle == null) {
        lst.clear()
    } else {
        lst.remove(handle.idx)
        var idx = 0;
        for(hd <- lst) {
          hd.idx = idx
          idx += 1
        }
    }
  }

  def fireEvent(evType:GameEventType,isCapture:Boolean):Unit = {
    val dic = if(isCapture) captureEvents else bubbleEvents;
    if(dic.contains(evType.id)) {
      dic(evType.id).foreach(f => f.callFn())
    }
  }
}

object EventNode {
  implicit val eventNodeComp: Component[EventNode] = new Component[EventNode] {
    override def addToEntity(e: Entity): EventNode = {
      Foreign.addEventNode(e.id)
      val ret = new EventNode(e)
      EventSystem.addEventNode(ret)
      ret
    }
    override val key:String = "EventNode"
  }
}


class EventHandle(var callFn:() =>Unit,var idx:Int)