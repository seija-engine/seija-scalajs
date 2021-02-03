package seija.ui.controls

import seija.ui.ControlCreator
import seija.data.SExpr


object Dialog {
    implicit val imageCreator:ControlCreator[Dialog] = new ControlCreator[Dialog] {
        val name: String = "Dialog"
        def init(): Unit = {}
        def create(): Dialog = new Dialog
    }
}

class Dialog extends Frame {
    override def handleEvent(evKey: String, evData: scala.scalajs.js.Array[SExpr]): Unit = {
        evKey match {
            case ":close-dialog" =>
                this.destroy()
            case _ => super.handleEvent(evKey,evData);
        }
    }
}