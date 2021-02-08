package seija.ui.controls

import seija.ui.Control
import seija.ui.ControlParams
import seija.core.Foreign
import seija.s2d.layout.ContentView
import seija.ui.comps.LayoutViewComp
import seija.ui.ControlCreator
import seija.core.Entity
import seija.s2d.TextRender
import seija.s2d.assets.Font
import seija.core.Transform
import seija.s2d.Rect2D

object RawInput {
    implicit val rawInputCreator:ControlCreator[RawInput] = new ControlCreator[RawInput] {
        val name: String = "RawInput"
        def init(): Unit = {}
        def create(): RawInput = new RawInput
    }
}

class RawInput extends Control with LayoutViewComp {
    var labelEntity:Option[Entity] = None
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        val newEntity = this.entity.get
        Foreign.attachRawInput(newEntity.id)
        val view = newEntity.addRawComp[ContentView](new ContentView(newEntity))
        newEntity.addRawComp[Transform](new Transform(newEntity))
        newEntity.addRawComp[Rect2D](new Rect2D(newEntity))
        this._view = Some(view)
        initLayoutView(this,view,params)
        labelEntity = Some(new Entity(Foreign.getRawInputLabel(newEntity.id)));
       
        val textRender = labelEntity.get.addRawComp[TextRender](new TextRender(labelEntity.get));

        initProperty[Int]("labelFont",params.paramStrings,None,Some(font => {
            textRender.setFont(new Font(font))
        }))
        
    }
}