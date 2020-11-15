package seija.ui

import seija.core.Entity
import seija.data.{SExpr, SExprParser,SExprInterp, XmlNode,SContent}

class UITemplate(val parent:SContent) {
  val sContext:SContent = new SContent(Some(parent))
}


object UITemplate {
  def create(xmlNode:XmlNode):UITemplate = {
    val newTemplate = new UITemplate(Control.sContent)
    if(xmlNode.children.isEmpty) {
      return newTemplate
    }
    xmlNode.children.get.head match {
      case node if node.tag == "Entity" =>
        this.parseEntity(node,newTemplate)
      case node =>
    }
    newTemplate
  }

  def parseEntity(xmlNode: XmlNode,tmpl:UITemplate):Entity = {
    val newEntity = Entity.New()
    if(xmlNode.children.isDefined) {
      for(node <- xmlNode.children.get) {
        node.tag match {
          case "Components" =>
            node.children.foreach(arr => {
              for(compNode <- arr) {
                UIComponent.attach(newEntity,compNode,tmpl)
              }
            })
          case nodeTagName =>
        }
      }
    }
    newEntity
  }


}