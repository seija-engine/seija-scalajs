package seija.ui2.controls
import seija.core.Entity
import seija.data.{DynObject, SContent, SExpr, SExprInterp, SNFunc, SNil, SUserData, SVector, XmlNode}
import seija.ui2.{Control, ControlCreator, UISystem, UITemplate}
import slogging.LazyLogging

import scala.scalajs.js
import scala.scalajs.js.Dictionary

case class MenuItemData(val text:String,val children:js.Array[MenuItemData],var index:Int = 0)
class MenuItem extends Control {
  override def init(): Unit = {
    this.property.put("isSelect",false)
    super.init()
  }

  override def handleEvent(evKey: String, evData: js.Array[SExpr]): Unit = {
    this.setProperty("isSelect",true)
  }
}

class Menu extends Control with LazyLogging {
  var itemTemplate:Option[XmlNode] = None
  def selectIndex:Int = this.property("selectIndex").asInstanceOf[Int]

  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
    this.setLispParam[js.Array[MenuItemData]]("dataSource",params,Some(js.Array()))
    this.property.put("selectIndex",-1)
  }

  override def setTemplates(temples: Dictionary[XmlNode]): Unit = {
    super.setTemplates(temples)
    this.itemTemplate = temples.get("ItemTemplate")

  }

  override def init(): Unit = {
    super.init()
    val slotEntity:Option[Entity] = this.template.get.slots.get("Children")
    if (slotEntity.isEmpty) {
      logger.error("Slot.Children not found")
      return
    }
    val menus:js.Array[MenuItemData] = this.property("dataSource").asInstanceOf[js.Array[MenuItemData]]
    var idx = 0
    for(topItem <- menus) {
      topItem.index = idx
      createMenuItem(topItem,slotEntity.get)
      idx += 1
    }
  }

  def createMenuItem(data:MenuItemData,slot: Entity):Unit = {
    val newItem = new MenuItem()
    newItem.template = Some(new UITemplate(this.itemTemplate.get,newItem))
    newItem.nsDic = this.nsDic
    newItem.dataContent = Some(data)

    newItem.init()
    newItem.setParent(Some(this))
    newItem.entity.get.setParent(Some(slot))
  }

  override def handleEvent(evKey: String, evData: js.Array[SExpr]): Unit = {
    evKey match {
      case ":click-menu" =>
        val index = evData(0).caseInt()
        println(index)
    }
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
