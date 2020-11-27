package seija.ui2.controls
import seija.data.{SExpr, SString, XmlNode}
import seija.math.Vector3
import seija.ui2.{Control, UITemplate}
import seija.data.CoreRead._

import scala.scalajs.js
import scala.scalajs.js.Dictionary
class CheckBox extends Control {
 
  def checked:Boolean = this.property.get("Checked").getOrElse(false).asInstanceOf[Boolean]
  def checked_= (newValue:Boolean):Unit = this.setProperty("Checked",newValue)

  def enable:Boolean = this.property.get("Enable").getOrElse(true).asInstanceOf[Boolean]
  def enable_= (newValue:Boolean):Unit = this.setProperty("Enable",newValue)


  override def setParams(params: Dictionary[String]): Unit = {
    this.setParam[Boolean]("Checked",params,Some(false))
    this.setParam[Boolean]("Enable",params,Some(true))
    this.setParam[Vector3]("position",params,Some(Vector3.zero))
  }

  override def handleEvent(evData: js.Array[SExpr]): Unit = {
    evData.head.castKeyword() match {
      case ":ClickCheck" =>
        if(this.enable) {
          this.checked = !this.checked
        }
      case _ => super.handleEvent(evData)
    }
  }
}