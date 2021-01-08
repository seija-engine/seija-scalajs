package seija.ui.controls
import seija.data.XmlExt._
import seija.ui.Control
import seija.ui.ControlCreator
import seija.ui.ControlParams
import seija.core.Entity
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView
import seija.ui.UISystem
import seija.ui.comps.LayoutViewComp

object Frame {
    implicit val imageCreator:ControlCreator[Frame] = new ControlCreator[Frame] {
        val name: String = "Frame"
        def init(): Unit = {}
        def create(): Frame = new Frame
    }
}

class Frame extends Control with LayoutViewComp {
    override def OnInit(parent: Option[Control], params: ControlParams,ownerControl:Option[Control] = None) {
        this.slots.put("Children",this)
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        val contentView = entity.addComponent[ContentView]()
        initLayoutView(this,contentView,params)
    }
}