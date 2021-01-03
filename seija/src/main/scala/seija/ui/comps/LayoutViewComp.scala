package seija.ui.comps
import seija.ui.Control
import seija.s2d.layout.Thickness
import seija.ui.ControlParams
import seija.s2d.layout.LayoutView
import seija.s2d.layout.LayoutAlignment._
import seija.math.Vector2
import seija.math.Vector3

trait LayoutViewComp {
    def initLayoutView(control:Control,view:LayoutView,param:ControlParams) {
        control.addPropertyLister[Thickness]("margin",(margin) => {
            view.setMargin(margin)
        })
        control.addPropertyLister[Thickness]("padding",(padding) => {
            view.setPadding(padding)
        })
        control.addPropertyLister[LayoutAlignment]("hor",(hor) => {
            view.setHor(hor)
        })
        control.addPropertyLister[LayoutAlignment]("ver",(ver) => {
            view.setVer(ver)
        })
        control.addPropertyLister[Float]("width",(w) => {
            view.setSize(Vector2.New(w,view.size.y) )
        })
        control.addPropertyLister[Float]("height",(h) => {
            view.setSize(Vector2.New(view.size.x,h) )
        })
        control.addPropertyLister[Vector3]("position",(pos) => {
            view.setPosition(pos)
        })
        control.initProperty[Thickness]("margin",param.paramStrings,None)
        control.initProperty[Thickness]("padding",param.paramStrings,None)
        control.initProperty[LayoutAlignment]("hor",param.paramStrings,None)
        control.initProperty[LayoutAlignment]("ver",param.paramStrings,None)
        control.initProperty[Vector3]("position",param.paramStrings,None)
        control.initProperty[Float]("width",param.paramStrings,None)
        control.initProperty[Float]("height",param.paramStrings,None)
    }
}