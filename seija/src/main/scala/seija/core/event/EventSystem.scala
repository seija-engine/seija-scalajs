package seija.core.event

import seija.core.Entity

import scala.collection.mutable
import scala.scalajs.js;
object EventSystem {
  var globalCallbacks:js.Array[() => Unit] = js.Array()
  var nodeEvents:mutable.HashMap[Int,EventNode] = mutable.HashMap();

  def handleEvent(event:js.Array[js.Any]):Unit = {
    val typId = event(0).asInstanceOf[Int];
    typId match {
      case 0 =>
        this.globalCallbacks.foreach(f => f())
      case 1 =>
        val entityId = event(1).asInstanceOf[Int]
        if(nodeEvents.contains(entityId)) {
          val evTypeId = event(2).asInstanceOf[Int];
          val ex0:Boolean = if (event(3).asInstanceOf[Int] == 0)  false else true;
          nodeEvents(entityId).fireEvent(GameEventType(evTypeId),ex0);
        }
      case 3 =>
        val eid = event(1).asInstanceOf[Int];
        if(nodeEvents.contains(eid) && nodeEvents(eid).entity.parent.isDefined) {
          this.unRegEventNode(eid)
        }
      case _ => ()
    }

  }

  def regKeyBoard(f:() => Unit):Int = {
    this.globalCallbacks.push(f)
  }

  def removeKeyBoard(f:() => Unit):Unit = {
    var idx = 0;
    for(cb <- this.globalCallbacks) {
       if(cb == f) {
         this.globalCallbacks.remove(idx)
         return;
       }
      idx +=1;
     }
  }

  def regEventNode(eventNode: EventNode):Unit = {
    if(!this.nodeEvents.contains(eventNode.entity.id)) {
      this.nodeEvents.put(eventNode.entity.id,eventNode)
    }
  }

  def unRegEventNode(eId:Int):Unit = {
    this.nodeEvents.remove(eId)
  }
}

object GameEventType extends Enumeration {
  type GameEventType = Value
  val TouchStart: GameEventType = Value(0)
  val TouchEnd: GameEventType = Value(1)
  val Click: GameEventType = Value(2)
  val MouseMove: GameEventType = Value(3)
  val MouseEnter: GameEventType = Value(4)
  val MouseLeave: GameEventType = Value(5)
  val KeyBoard: GameEventType = Value(6)
}
