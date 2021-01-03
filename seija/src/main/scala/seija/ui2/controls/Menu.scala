package seija.ui2.controls
import seija.core.{Entity,Time}
import seija.data._
import seija.data.Read._
import seija.ui2.{Control, ControlCreator, UISystem, UITemplate}
import slogging.LazyLogging

import scala.scalajs.js
import scala.scalajs.js.Dictionary
import seija.core.event.EventSystem
import seija.core.event.GameEventType
import seija.core.event.GameEvent
import seija.ui2.ControlParams

case class MenuItemData(text:String,children:js.Array[MenuItemData],var index:Int = 0)

class MenuItem extends Control {
  def isSelect:Boolean = this.property("isSelect").asInstanceOf[Boolean]
  def isSelect_= (newVal:Boolean): Unit = this.setProperty("isSelect",newVal)


  override def init(param:ControlParams,parent:Option[Control] = None): Unit = {
    this.property.put("isSelect",false)
    super.init(param)
  }

  override def handleEvent(evKey: String, evData: js.Array[SExpr]): Unit = {
    this.parent.foreach(_.handleEvent(evKey,evData))
  }
}

class Menu extends Control with LazyLogging {
  var itemTemplate:Option[XmlNode] = None
  var selectIndex:Int = -1
  val items:js.Array[MenuItem] = js.Array()
  var isShowing:Boolean = false
  var globalHandle:Option[IndexedRef] = None
  var lastClickFrame:Int = -1
  var contextMenu:Option[ContextMenu] = None

  def contextMenuPath:String = this.property("contextMenu").asInstanceOf[String]
  def menuDatas:js.Array[MenuItemData] = this.property("dataSource").asInstanceOf[js.Array[MenuItemData]]

  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
    this.setLispParam[js.Array[MenuItemData]]("dataSource",params,Some(js.Array()))
    this.setParam[String]("contextMenu",params,Some("core/ContextMenu.xml"))
  }

  override def setTemplates(temples: Dictionary[XmlNode]): Unit = {
    super.setTemplates(temples)
    this.itemTemplate = temples.get("ItemTemplate")
    
  }

  def unSelectAll() {
    for(item <- this.items;if item.isSelect) {
      item.isSelect = false
    }
  }

  def selectMenu(index:Int) {
    if(index == this.selectIndex) return
    this.unSelectAll()
    val curMenuItem = this.items(index)
    curMenuItem.isSelect = true
    this.selectIndex = index
    this.contextMenu match {
      case Some(value) =>
        value.entity.get.setParent(this.items(index).entity)
      case None =>
        val newControl = UISystem.create(this.contextMenuPath,parent = Some(this)).toOption
        newControl.get.entity.get.setParent(this.items(index).entity)
        this.contextMenu = newControl.asInstanceOf[Option[ContextMenu]]
    }
  }

  def unSelectMenu(): Unit = {
    this.selectIndex = -1
    this.contextMenu.foreach(_.destroy())
    this.contextMenu = None
  }

  override def handleEvent(evKey: String, evData: js.Array[SExpr])  {
    evKey match {
      case ":click-menu" =>
        if(lastClickFrame == Time.frame()) return
        lastClickFrame = Time.frame()
        if(this.isShowing) {
          this.unSelectAll()
          this.isShowing = false;
          this.unSelectMenu()
          this.globalHandle.foreach(EventSystem.removeGlobalEvent(GameEventType.TouchStart,_))
          this.globalHandle = None

          return
        }
        this.isShowing = true
        this.globalHandle = EventSystem.addGlobalEvent(GameEventType.TouchStart,this.OnTouchStart)
        val index = evData(0).castSingleAny().asInstanceOf[Int]

        this.selectMenu(index)
      case ":menu-enter" =>
        val index = evData(0).castSingleAny().asInstanceOf[Int]
        if(this.isShowing) {
          this.selectMenu(index)
        }
    }
  }

  protected def OnTouchStart(ev:GameEvent) {
     if(lastClickFrame == Time.frame()) {
       return
     }
     lastClickFrame = Time.frame()
     this.unSelectAll()
     this.isShowing = false
     this.globalHandle.foreach(idx => {
       EventSystem.removeGlobalEvent(GameEventType.TouchStart,idx)
     })
    this.globalHandle = None
    this.unSelectMenu()
  }

  override def init(param:ControlParams,parent:Option[Control] = None)  {
    super.init(param)
    val slotEntity:Option[Entity] = this.template.get.slots.get("Children")
    if (slotEntity.isEmpty) {
      logger.error("Slot.Children not found")
      return
    }
    val menus:js.Array[MenuItemData] = this.menuDatas
    var idx = 0
    this.items.clear()
    for(topItem <- menus) {
      topItem.index = idx
      val item = createMenuItem(topItem,slotEntity.get)
      this.items.push(item)
      idx += 1
    }
  }

  def createMenuItem(data:MenuItemData,slot: Entity):MenuItem = {
    val newItem = new MenuItem()
    newItem.template = Some(new UITemplate(this.itemTemplate.get,newItem))
    newItem.nsDic = this.nsDic
    newItem.dataContent = Some(data)
    newItem.init(ControlParams())
    newItem.setParent(Some(this))
    newItem.entity.get.setParent(Some(slot))
    newItem
  }
}

object Menu {
  implicit val menuCreator:ControlCreator[Menu] = new ControlCreator[Menu] {
    override def name: String = "Menu"
    override def create(): Control = new Menu
    override def init(): Unit = {
      UISystem.setSExpr("menu-item",SNFunc(menuItem))
      DynObject.registerClass[MenuItemData]()
    }
  }

  def menuItem(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.evalToValue(e,Some(content)))
    val menuName = evalArgs(0).asInstanceOf[String]
    val children:js.Array[MenuItemData] = if(evalArgs.length > 1) {
      evalArgs(1).asInstanceOf[js.Array[MenuItemData]]
    } else { js.Array() }
    SUserData(MenuItemData(menuName,children))
  }
}
