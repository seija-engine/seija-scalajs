package seija.ui.controls

import seija.ui.Control
import seija.ui.ControlCreator
import seija.ui.ControlParams
import seija.data.Color
import seija.data.XmlExt._
import seija.ui.UITemplate

object Panel {
    implicit val imageCreator:ControlCreator[Panel] = new ControlCreator[Panel] {
        val name: String = "Panel"
        def init(): Unit = {}
        def create(): Panel = new Panel
    }
}

class Panel extends Control {
    var template:Option[UITemplate] = None
    override def init(parent: Option[Control], params: ControlParams) {
        super.init(parent,params)
        this.template = Some(UITemplate(params.paramXmls("Template"),this))
        this.initProperty[Color]("color",params.paramStrings,Some(Color.white))

        this.template.get.create()
    }
}