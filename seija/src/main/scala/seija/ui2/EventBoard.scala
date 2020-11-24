package seija.ui2
import seija.core.{BaseComponent, Component, Entity, Foreign}
import seija.data.SExpr
import seija.s2d.Rect2D

import scala.scalajs.js

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

class EventBoard(val name:String) {
  val eventDic:js.Dictionary[js.Array[SExpr => Unit]] = js.Dictionary()
  def fire(key:String,eventData:SExpr):Unit = {
    this.eventDic.get(key) match {
      case Some(value) => value.foreach(f => f(eventData))
      case None =>
    }
  }

  def register(key:String,func:SExpr => Unit):Unit = {
    if(!this.eventDic.contains(key)) {
      this.eventDic.put(key,js.Array())
    }
    this.eventDic(key).push(func)
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