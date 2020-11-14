package seija.ui
import seija.core.Entity
import seija.data.XmlNode

import scalajs.js
trait UIControl {

}

trait UIComponent {
  def attach(entity: Entity,xmlNode:XmlNode,tmpl:UITemplate):Unit
}

object UIComponent {
  private var comps:js.Dictionary[UIComponent] = js.Dictionary()

  def attach(entity: Entity, xmlNode: XmlNode,tmpl:UITemplate):Unit = {
    this.comps.get(xmlNode.tag) match {
      case Some(value) => value.attach(entity,xmlNode,tmpl)
      case None => println("not register " + xmlNode.tag)
    }
  }

  def register(compName: String,comp:UIComponent):Unit = {
    this.comps.put(compName,comp)
  }
}