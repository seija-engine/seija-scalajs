package seija.ui.controls
import seija.ui.ControlCreator
import seija.ui.Control
import seija.ui.ControlParams
import seija.ui.comps.LayoutViewComp
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView

object SelectBox {
    implicit val selectBoxCreator:ControlCreator[SelectBox] = new ControlCreator[SelectBox] {
        val name: String = "SelectBox"
        def init(): Unit = {}
        def create(): SelectBox = new SelectBox
    }
}

class SelectBox extends Control with LayoutViewComp {
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        val view = entity.addComponent[ContentView]()

        initLayoutView(this,view,params);
    }
}