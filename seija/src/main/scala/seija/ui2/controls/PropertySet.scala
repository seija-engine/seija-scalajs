package seija.ui2.controls
import seija.ui2.Control
import scala.scalajs.js;
object PropertySet {
  def setEvent(control:Control,params:js.Dictionary[String]):Unit = {
    control.setEventParam("OnClick",params)
  }

  def setLayout(control: Control,params:js.Dictionary[String]):Unit = {

  }
}