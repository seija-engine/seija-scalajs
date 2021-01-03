package seija.ui2.controls
import scala.scalajs.js
import seija.data.XmlExt._
import seija.ui2.Control
import seija.ui2.ControlCreator
import seija.ui2.ControlParams
import seija.core.Entity
import seija.ui2.UISystem

class EntityControl extends Control {

  override def init(param:ControlParams,parent:Option[Control] = None) {
      this.entity = Some(Entity.New(parent.flatMap(_.entity)))
      this.slots.put("Children",this.entity.get)
      println(param.paramXmls.keySet)
      val components = param.paramXmls.get("Components")
      if(components.isDefined) {
        for(compXml <- components.get.children.getOrElse(js.Array())) {
            UISystem.getUIComp(compXml.tag) match {
                case Some(value) =>  value.attach(this.entity.get,compXml,this)
                case None => logger.error(s"not found ${compXml.tag} UIComponent")
            }
        }
      }
  }
}

object EntityControl {
    implicit val entityCreator:ControlCreator[EntityControl] = new ControlCreator[EntityControl] {
        override def name: String = "EntityControl"
        override def create(): Control = new EntityControl
        override def init(): Unit = {}
    }
}