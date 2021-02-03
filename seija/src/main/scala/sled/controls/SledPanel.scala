package sled.controls

import seija.ui.controls.Frame
import seija.ui.{Control, ControlParams}
import seija.ui.ControlCreator
import seija.data.SExpr
import seija.ui.UISystem

object SledPanel {
    implicit val imageCreator:ControlCreator[SledPanel] = new ControlCreator[SledPanel] {
        val name: String = "SledPanel"
        def init(): Unit = {}
        def create(): SledPanel = new SledPanel
    }
}

class SledPanel extends Frame {
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        super.OnInit(parent,params,ownerControl);
    }

    override def handleEvent(evKey: String, evData: scala.scalajs.js.Array[SExpr]): Unit = {
        evKey match {
            case ":click-menu" => 
                this.onClickMenu(evData(0).castString());
        }
    }


    def onClickMenu(menuName:String) {
        menuName match {
            case "newProject" =>
                 UISystem.createByFile("sled/SelectFile.xml",None,ControlParams(),None)
            case _ => logger.info(menuName)
        }
    }
}