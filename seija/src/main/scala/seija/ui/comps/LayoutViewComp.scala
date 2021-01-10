package seija.ui.comps
import seija.ui.Control
import seija.s2d.layout.Thickness
import seija.ui.ControlParams
import seija.s2d.layout.LayoutView
import seija.s2d.layout.LayoutAlignment._
import seija.math.Vector2
import seija.math.Vector3
import seija.s2d.layout.ViewType._

trait LayoutViewComp {
    def initLayoutView(control:Control,view:LayoutView,param:ControlParams) {
        control.initProperty[Thickness]("margin",param.paramStrings,None,Some((margin) => {
            view.setMargin(margin)
        }))
        control.initProperty[Thickness]("padding",param.paramStrings,None,Some((padding) => {
            view.setPadding(padding)
        }))
        control.initProperty[LayoutAlignment]("hor",param.paramStrings,None,Some((hor) => {
            view.setHor(hor)
        }))
        control.initProperty[LayoutAlignment]("ver",param.paramStrings,None,Some((ver) => {
            view.setVer(ver)
        }))
        control.initProperty[Vector3]("position",param.paramStrings,None,Some((pos) => {
            view.setPosition(pos)
        }))
        control.initProperty[Float]("width",param.paramStrings,None,Some((w) => {
            view.setSize(Vector2.New(w,view.size.y) )
        }))
        control.initProperty[Float]("height",param.paramStrings,None,Some((h) => {
            view.setSize(Vector2.New(view.size.x,h) )
        }))
        control.initProperty[ViewType]("viewType",param.paramStrings,None,Some((vt) => {
            view.setViewType(vt)
        }))
    }
}