package seija.ui
import seija.data.XmlNode
import scala.scalajs.js
case class UITemplate(val xmlNode:XmlNode,ownerControl:Control) {
    def create() {
        for(childNode <- xmlNode.children.getOrElse(js.Array())) {
          if(childNode.tag.startsWith("Slot.")) {
            ownerControl.slots.put(childNode.tag.substring("Slot.".length()),ownerControl)
          } else {
            val control = UISystem.createByXml(childNode,Some(ownerControl),ControlParams(),Some(ownerControl))
          }
        }
    }  
}