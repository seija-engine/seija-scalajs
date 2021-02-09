package seija.ui.controls

import seija.ui.Control
import seija.ui.ControlCreator
import seija.ui.ControlParams
import seija.s2d.layout.ContentView
import seija.ui.comps.LayoutViewComp
import seija.core.Transform
import seija.s2d.Rect2D

object ListBox {
    implicit val listBoxCreator:ControlCreator[ListBox] = new ControlCreator[ListBox] {
        val name: String = "ListBox"
        def init(): Unit = {}
        def create(): ListBox = new ListBox
  }
}

class ListBox extends Control with LayoutViewComp {
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        val newEntity = this.entity.get
        newEntity.addComponent[Transform]()
        newEntity.addComponent[Rect2D]()
        val view = newEntity.addComponent[ContentView]()
        initLayoutView(this,view,params)


        //initProperty[js.Array[Any]]("dataSource",params.paramStrings,None,None)
    }
}