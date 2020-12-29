package seija.ui2.controls
import seija.core.Entity
import seija.data.{SExpr, XmlNode}
import seija.ui2.Control
import slogging.LazyLogging

import scala.scalajs.js
import scala.scalajs.js.Dictionary

class Menu extends Control with LazyLogging {
  var itemTemplate:Option[XmlNode] = None

  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
  }

  override def setTemplates(temples: Dictionary[XmlNode]): Unit = {
    this.itemTemplate = temples.get("ItemTemplate")
  }

  override def init(): Unit = {
    super.init()
    val slotEntity:Option[Entity] = this.template.get.slots.get("Children")
    if (slotEntity.isEmpty) {
      logger.error("Slot.Children not found")
      return
    }


  }

  override def handleEvent(evKey: String, evData: js.Array[SExpr]): Unit = {
    evKey match {
      case ":menu" =>

    }
  }
}
