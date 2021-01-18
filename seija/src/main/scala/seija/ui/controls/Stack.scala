package seija.ui.controls

import seija.ui.Control
import seija.ui.ControlParams
import seija.core.Transform
import seija.s2d.Rect2D
import seija.ui.ControlCreator
import seija.s2d.layout.StackLayout
import seija.ui.comps.LayoutViewComp
import seija.s2d.layout.Orientation._
import seija.data.XmlExt._
import seija.ui.comps.EventNodeComp

object Stack {
    implicit val stackCreator:ControlCreator[Stack] = new ControlCreator[Stack] {
        val name: String = "Stack"
        def init(): Unit = {}
        def create(): Stack = new Stack
    }
}

class Stack extends Control with LayoutViewComp with EventNodeComp {
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        this.slots.put("Children",this)
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        val stack = entity.addComponent[StackLayout]()
        this._view = Some(stack)
        initLayoutView(this,stack,params)
        initEventComp(this,params)
        initProperty[Float]("spacing",params.paramStrings,None,Some((spacing) => {
            stack.setSpacing(spacing)
        }))

        initProperty[Orientation]("orientation",params.paramStrings,None,Some((orientation) => {
            stack.setOrientation(orientation)
        }))

        
    }
}