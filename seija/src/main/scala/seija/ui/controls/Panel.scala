package seija.ui.controls

import seija.ui.Control
import seija.ui.ControlCreator
import seija.ui.ControlParams
import seija.data.Color
import seija.data.XmlExt._
import seija.ui.UITemplate
import seija.core.Entity
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView
import seija.ui.comps.LayoutViewComp

object Panel {
    implicit val panelCreator:ControlCreator[Panel] = new ControlCreator[Panel] {
        val name: String = "Panel"
        def init(): Unit = {}
        def create(): Panel = new Panel
    }
}

class Panel extends Control with LayoutViewComp {
    var template:Option[UITemplate] = None
    override def OnInit(parent: Option[Control], params: ControlParams,ownerControl:Option[Control] = None) {
        this.slots.put("Children",this)
        val newEntity = this.entity.get
        newEntity.addComponent[Transform]()
        newEntity.addComponent[Rect2D]()
        val contentView = newEntity.addComponent[ContentView]()
        
        this._view = Some(contentView)
        this.initProperty[Color]("color",params.paramStrings,Some(Color.white),None)
        this.initLayoutView(this,contentView,params)
        
    }
}