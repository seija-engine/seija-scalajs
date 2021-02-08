package seija.ui.controls

import seija.ui.ControlCreator
import seija.ui.Control
import seija.ui.ControlParams
import seija.s2d.layout.ContentView
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.Transparent
import seija.ui.comps.LayoutViewComp
import seija.ui.comps.EventNodeComp
import seija.core.event.EventNode
import seija.data.SExpr

object Button {
    implicit val ButtonCreator:ControlCreator[Button] = new ControlCreator[Button] {
        val name: String = "Button"
        def init(): Unit = {}
        def create(): Button = new Button
    }
}

object ButtonState {
   val Normal = "Normal"
   val Hover  = "Hover"
   val Disable = "Disable"
   val Press = "Press"
}

class Button extends Control with LayoutViewComp with EventNodeComp {
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        val newEntity = this.entity.get
        val view = newEntity.addComponent[ContentView]();
        this._view = Some(view)
        newEntity.addComponent[Transform]()
        newEntity.addComponent[Rect2D]()
        newEntity.addComponent[Transparent]()

        initProperty[String]("buttonState",params.paramStrings,Some(ButtonState.Normal),None);
        initProperty[String]("text",params.paramStrings,Some(""),None)
        initLayoutView(this,view,params)
        initEventComp(this,params)
    }

    override def handleEvent(evKey: String, evData: scala.scalajs.js.Array[SExpr]): Unit = {
        evKey match {
            case ":btn-enter" =>
                this.setProperty("buttonState",ButtonState.Hover)
            case ":btn-exit"  =>
                this.setProperty("buttonState",ButtonState.Normal)
            case ":btn-touch-end" =>
                this.setProperty("buttonState",ButtonState.Hover)
            case ":btn-touch" =>
                this.setProperty("buttonState",ButtonState.Press)
            case _ =>
        }
    }
}