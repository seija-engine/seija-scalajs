package seija.ui2
import seija.core.Entity
import seija.data.{SContent, SExprInterp, SList, SUserData}

import scalajs.js

class Control extends IBehavior {
    val sContent:SContent = new SContent(UISystem.cContent)
    var entity:Option[Entity] = None
    var Property:js.Dictionary[Any] = js.Dictionary()
    var template:Option[UITemplate] = None
    var dataContent:Option[Any] = None

    def init():Unit = {
      this.sContent.set("control",SUserData(this))
    }

    def Enter():Unit = {
       if(this.template.isDefined) {
           this.template.get.create() match {
               case Left(value) => println(value)
               case Right(value) => this.entity = Some(value)
           }
       }
    }

}