package seija.ui2

import seija.core.Entity
import seija.data.XmlNode

class UITemplate(val xmlNode: XmlNode,val control: Control) {
  def create():Either[String,Entity]  = {
    if(xmlNode.children.isEmpty || xmlNode.children.get.length == 0) {
      return  Left("template need children")
    }
    val firstNode = xmlNode.children.get(0)
    xmlNode.children.get(0).tag match {
      case "Entity" => Right(this.parseEntity(firstNode))
      case _ => ???
    }
  }

  def parseEntity(xmlNode: XmlNode) :Entity = {
    val newEntity = Entity.New()
    if(xmlNode.children.isDefined) {
      for(node <- xmlNode.children.get) {
        node.tag match {
          case "Components" =>
            node.children.foreach(arr => {
              for(compNode <- arr) {
                 UISystem.getUIComp(compNode.tag).foreach(_.attach(newEntity,compNode,this))
              }
            })
          case _ =>
        }
      }
    }
    newEntity
  }
}
