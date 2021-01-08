package seija.ui.controls

import seija.ui.Control
import seija.ui.ControlCreator
import seija.ui.ControlParams
import seija.core.Entity
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView
import seija.ui.comps.LayoutViewComp

object Menu {
    implicit val menuCreator:ControlCreator[Menu] = new ControlCreator[Menu] {
        val name: String = "Menu"
        def init(): Unit = {}
        def create(): Menu = new Menu
    }
}

class Menu extends Control with LayoutViewComp {
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        val entity = Entity.New(parent.flatMap(_.entity))
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        val contentView = entity.addComponent[ContentView]()
        initLayoutView(this,contentView,params)


    }

    override def OnEnter(): Unit = {
        logger.info("Menu OnEnter")
    }
}