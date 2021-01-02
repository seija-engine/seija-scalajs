package seija.core.event
import seija.core.Entity
import GameEventType.GameEventType
import seija.data.Read
import seija.math.Vector3
import scala.collection.mutable
import scala.scalajs.js
import slogging.LazyLogging
import seija.data.IndexedRef
object EventSystem extends LazyLogging {
  private val globalEvents:mutable.HashMap[GameEventType,js.Array[IndexedRef]] = mutable.HashMap()
  private val rmGlobals:js.Array[(GameEventType,IndexedRef)] = js.Array()
  private var nodeEvents:mutable.HashMap[Int,EventNode] = mutable.HashMap();

  def handleEvent(eventData:js.Array[js.Any]):Unit = {
    val typId = eventData(0).asInstanceOf[Int];
    typId match {
      case 0 =>
        val evTypeId = eventData(2).asInstanceOf[Int];
        val gameEv = GameEvent.fromJs(evTypeId,eventData(4).asInstanceOf[js.Array[Any]]);
        globalEvents.get(GameEventType(evTypeId)).foreach(arr => {
          arr.foreach(ev => ev.value.asInstanceOf[GameEvent => Unit](gameEv))
        })
        for((evType,idxRef) <- this.rmGlobals) {
          if(this.globalEvents.contains(evType)) {
            val arr = this.globalEvents(evType)
            arr.remove(idxRef.index)
            for(idx <- 0 until arr.length) {
              arr(idx).index = idx
            }
          }
        }
        this.rmGlobals.length = 0
      case 1 =>
        val entityId = eventData(1).asInstanceOf[Int]
        if(nodeEvents.contains(entityId)) {
          val evTypeId = eventData(2).asInstanceOf[Int];
          val ex0:Boolean = if (eventData(3).asInstanceOf[Int] == 0)  false else true;
          nodeEvents(entityId).fireEvent(GameEventType(evTypeId),ex0);
        }
     
      case _ => ()
    }
  }

  

  def addGlobalEvent(evType:GameEventType,callFn:(GameEvent) => ()): Option[IndexedRef] = {
    if(!this.globalEvents.contains(evType)) {
      this.globalEvents.put(evType,js.Array())
    }
    for(ev <- this.globalEvents(evType)) {
      if(ev.value == callFn) return None
    }
    val refValue = IndexedRef(this.globalEvents(evType).length,callFn)
    this.globalEvents(evType).push(refValue)
    Some(refValue)
  }

  def removeGlobalEvent(evType:GameEventType,idxRef:IndexedRef) {
     if(!this.globalEvents.contains(evType)) return
     this.rmGlobals.push((evType,idxRef))
  }

  def addEventNode(eventNode: EventNode):Unit = {
    if(!this.nodeEvents.contains(eventNode.entity.id)) {
      this.nodeEvents.put(eventNode.entity.id,eventNode)
    }
  }

  def removeEventNode(eId:Int):Unit = {
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

  implicit val gameEventTypeRead: Read[GameEventType] = {
    case "Click" => Some(GameEventType.Click)
    case "TouchStart" => Some(GameEventType.TouchStart)
    case "TouchEnd" => Some(GameEventType.TouchEnd)
    case "MouseMove" => Some(GameEventType.MouseMove)
    case "MouseEnter" => Some(GameEventType.MouseEnter)
    case "MouseLeave" => Some(GameEventType.MouseLeave)
    case "KeyBoard"   => Some(GameEventType.KeyBoard)
    case _ => Some(GameEventType.Click)
  }
}

sealed trait GameEvent
object GameEvent {
  def fromJs(typeId:Int,values:js.Array[Any]):GameEvent = {
    val evType = GameEventType(typeId)
    evType match {
      case GameEventType.TouchStart => TouchStart(values(0).asInstanceOf[Float],values(1).asInstanceOf[Float])
      case GameEventType.TouchEnd => TouchEnd(values(0).asInstanceOf[Float],values(1).asInstanceOf[Float])
      case GameEventType.Click => Click(values(0).asInstanceOf[Float],values(1).asInstanceOf[Float])
      case GameEventType.KeyBoard => KeyBoard(values(0).asInstanceOf[Int],values(1).asInstanceOf[Boolean])
    }
  }
}
case class TouchStart(val x:Float,val y:Float) extends GameEvent
case class TouchEnd(val x:Float,val y:Float) extends GameEvent
case class Click(val x:Float,val y:Float) extends GameEvent
case class Move(val x:Float,val y:Float) extends GameEvent
case class MouseEnter(val x:Float,val y:Float) extends GameEvent
case class MouseLeave(val x:Float,val y:Float) extends GameEvent
case class KeyBoard(val code:Int,val isDown:Boolean) extends GameEvent