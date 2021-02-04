package seija.ui.controls

import seija.ui.Control
import seija.ui.ControlCreator
import seija.ui.ControlParams
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView
import seija.core.Entity
import seija.s2d.Transparent
import seija.s2d.TextRender
import seija.s2d.layout.LayoutView
import seija.ui.comps.LayoutViewComp
import seija.s2d.assets.Font

object Input {
    implicit val inputCreator:ControlCreator[Input] = new ControlCreator[Input] {
        val name: String = "Input"
        def init(): Unit = {}
        def create(): Input = new Input
    }
}

class Input extends Control with LayoutViewComp {
    var textRender:Option[TextRender] = None 
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        val view = entity.addComponent[ContentView]()
        this._view = Some(view)
        
        val labelEntity = Entity.New(Some(entity))
        labelEntity.addComponent[Transform]()
        labelEntity.addComponent[Rect2D]()
        labelEntity.addComponent[Transparent]()
        labelEntity.addComponent[LayoutView]()
        this.textRender = Some(labelEntity.addComponent[TextRender]())
        this.textRender.get.setText("|111111111111")

        initProperty[Int]("font",params.paramStrings,None,Some(font => {
            this.textRender.get.setFont(new Font(font))
        }))
        initProperty[Int]("fontSize",params.paramStrings,None,Some(fontSize => {
            this.textRender.get.setFontSize(fontSize)
        }))
        this.initLayoutView(this,view,params);
    }
}