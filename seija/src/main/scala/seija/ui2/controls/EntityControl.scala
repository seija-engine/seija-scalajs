package seija.ui2.controls

import seija.ui2.Control
import seija.ui2.ControlCreator

class EntityControl extends Control {
    
}

object EntityControl {
    implicit val entityCreator:ControlCreator[EntityControl] = new ControlCreator[EntityControl] {
        override def name: String = "EntityControl"
        override def create(): Control = new EntityControl
        override def init(): Unit = {}
    }
}