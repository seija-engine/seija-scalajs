package seija.ui.controls

import seija.ui.Control
import seija.ui.comps.LayoutViewComp
import seija.ui.ControlParams
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.layout.LayoutView
import seija.s2d.Transparent
import seija.ui.ControlCreator
import seija.s2d.TextRender
import seija.data.Color
import seija.s2d.assets.Font
import seija.ui.comps.EventNodeComp
import seija.data.AnchorAlign._
import seija.s2d.LineMode._

object Label {
  implicit val labelCreator:ControlCreator[Label] = new ControlCreator[Label] {
        val name: String = "Label"
        def init(): Unit = {}
        def create(): Label = new Label
  }
}

class Label extends Control with LayoutViewComp with EventNodeComp {
    override def OnInit(parent: Option[Control], params: ControlParams,ownerControl:Option[Control] = None) {
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        entity.addComponent[Transparent]()
        val view = entity.addComponent[LayoutView]()
        val label = entity.addComponent[TextRender]()
        this._view = Some(view)
        initLayoutView(this,view,params)
        initEventComp(this,params)
        initProperty[String]("text",params.paramStrings,Some("Text"),Some(text => {
            label.setText(text)
        }))
        initProperty[Color]("color",params.paramStrings,Some(Color.white),Some(color => {
            label.color = color
        }))
        initProperty[Int]("font",params.paramStrings,None,Some(font => {
            label.setFont(new Font(font))
        }))
        initProperty[Int]("fontSize",params.paramStrings,None,Some(fontSize => {
            label.setFontSize(fontSize)
        }))
        initProperty[AnchorAlign]("anchor",params.paramStrings,None,Some(anchor => {
            label.setAnchor(anchor)
        }))
        initProperty[LineMode]("lineMode",params.paramStrings,None,Some(lineMode => {
            label.setLineMode(lineMode)
        }))
    }
}