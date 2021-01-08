package seija.ui
import seija.data.XmlNode
import scala.scalajs.js
import slogging.LazyLogging
case class UITemplate(val xmlNode:XmlNode,ownerControl:Control) extends LazyLogging {
    def create() {
        for(childNode <- xmlNode.children.getOrElse(js.Array())) {
          if(childNode.tag.startsWith("Slot.")) {
            ownerControl.slots.put(childNode.tag.substring("Slot.".length()),ownerControl)
          } else {
            val control = UISystem.createByXml(childNode,Some(ownerControl),ControlParams(),Some(ownerControl))
            control match {
              case Left(errString) => logger.error(errString)
              case Right(value) => ()
            }
          }
        }
    }  
}