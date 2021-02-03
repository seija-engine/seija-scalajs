package seija.ui.controls
import seija.ui.ControlCreator
import seija.data.SExpr
import scalajs.js

object SelectFile {
    implicit val imageCreator:ControlCreator[SelectFile] = new ControlCreator[SelectFile] {
        val name: String = "SelectFile"
        def init(): Unit = {}
        def create(): SelectFile = new SelectFile
    }
}

class SelectFile extends Dialog {
    var rootPath:String = "/"
    override def handleEvent(evKey: String, evData: js.Array[SExpr]): Unit = {
        evKey match {
            case ":select-root" =>
                this.rootPath = evData(0).castString()
                logger.info("Select "+this.rootPath)
            case _ => super.handleEvent(evKey,evData)
        }
    }
}