package seija.ui2
import seija.core.{BaseComponent, Component, Entity, Foreign}
import seija.data.SExpr
import seija.s2d.Rect2D

import scala.scalajs.js
import seija.data.SKeyword

class EventBoardComponent(override val entity:Entity) extends BaseComponent(entity) {
  var eventBoard:Option[EventBoard] = None

  def initBoard(name:String):Unit = {
    this.eventBoard = Some(EventBoard.create(name))
  }

  override def onDetach(): Unit = {
    if(this.eventBoard.isDefined) {
      EventBoard.remove(this.eventBoard.get.name)
    }
  }
}

object EventBoardComponent {
  implicit val eventBoardComp: Component[EventBoardComponent] = new Component[EventBoardComponent] {
    override val key:String = "EventBoard"
    override def addToEntity(e: Entity): EventBoardComponent = {
      new EventBoardComponent(e)
    }
  }
}

class EventBoard(val name:String) extends IEventReceive {
  protected  val keyEventDic:js.Dictionary[js.Array[ (String,js.Array[SExpr]) => Unit]] = js.Dictionary()
  protected  val allEventList:js.Array[(String,js.Array[SExpr]) => Unit] = js.Array()

  override def handleEvent(evKey: String, evData:js.Array[SExpr]): Unit = {
    this.fire(evKey,evData)
  }

  def fire(key:String,eventData:js.Array[SExpr]):Unit = {
    println(s"[EventBoard] $key = $eventData")
    this.keyEventDic.get(key) match {
      case Some(value) => value.foreach(f => f(key,eventData))
      case None => ()
    }
    this.allEventList.foreach(f => {
      f(key,eventData)
    })
    
  }

  def register(key:String,func:(String,js.Array[SExpr]) => Unit):Unit = {
    if(!this.keyEventDic.contains(key)) {
      this.keyEventDic.put(key,js.Array())
    }
    this.keyEventDic(key).push(func)
  }

  def addEventRecv(eventRecv:IEventReceive):Unit = {
    this.registerAll(eventRecv.handleEvent);
  }

  def removeEventRecv(eventRecv:IEventReceive):Unit = {
    this.unRegisterAll(eventRecv.handleEvent)
  }

  def registerAll(f:(String,js.Array[SExpr]) => Unit):Unit = {
    this.allEventList.push(f)
  }
  
  def unRegisterAll(f:(String,js.Array[SExpr]) => Unit):Unit = {
    for(idx <- 0 to this.allEventList.length) {
      if(f == this.allEventList(idx)) {
        this.allEventList.remove(idx)
        return
      }
    }
  }
}

object EventBoard {
  val boards:js.Dictionary[EventBoard] = js.Dictionary()

  def create(name:String):EventBoard = {
    if(boards.contains(name)) {
      return boards(name)
    }
    val newBoard = new EventBoard(name)
    boards.put(name,newBoard)
    newBoard
  }

  def remove(name:String):Unit = {
    this.boards.remove(name)
  }
}