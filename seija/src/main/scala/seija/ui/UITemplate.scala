package seija.ui
import seija.data.XmlNode
import scala.scalajs.js
import slogging.LazyLogging
case class UITemplate(val xmlNode:XmlNode,ownerControl:Control) extends LazyLogging {
    
    def create() {
        var zIndex = 0
        for(childNode <- xmlNode.children.getOrElse(js.Array())) {
          if(childNode.tag.startsWith("Slot.")) {
            ownerControl.slots.put(childNode.tag.substring("Slot.".length()),ownerControl)
          } else {
            val eControl = UISystem.createByXml(childNode,Some(ownerControl),ControlParams(
              paramStrings = js.Dictionary("zIndex" -> zIndex.toString())
            ),Some(ownerControl))
            eControl match {
              case Left(errString) => logger.error(errString)
              case Right(control) =>
            }
            zIndex += 1
          }
        }
    }  
}