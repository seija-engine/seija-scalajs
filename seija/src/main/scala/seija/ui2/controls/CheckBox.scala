package seija.ui2.controls
import seija.data.{SExpr, SString, XmlNode}
import seija.math.Vector3
import seija.ui2.{Control, UITemplate}
import seija.data.CoreRead._

import scala.scalajs.js
import scala.scalajs.js.Dictionary
class CheckBox extends Control {
  override def init(): Unit = {
    super.init()
    this.property.put("Checked",false)
  }

  def checked:Boolean = this.property.get("Checked").getOrElse(false).asInstanceOf[Boolean]
  def checked_= (newValue:Boolean):Unit = this.setProperty("Checked",newValue)


  override def setParams(params: Dictionary[String]): Unit = {
    this.setParam[Boolean]("Checked",params,Some(false))
    this.setParam[Vector3]("position",params,Some(Vector3.New(0,100,0)))
  }

  override def handleEvent(evData: js.Array[SExpr]): Unit = {
    super.handleEvent(evData)
    this.checked = !this.checked
    val spriteName = if(this.checked) "checkbox-checked" else "checkbox-unchecked"
    this.emit(":UpdateSprite",SString(spriteName))
  }
}