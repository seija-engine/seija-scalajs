package seija.ui2.controls

import seija.ui2.Control
import scala.scalajs.js.Dictionary
import seija.data.CoreRead._
import seija.math.Vector3
import seija.data.Color

class LabelControl extends Control {
    override def setParams(params: Dictionary[String]): Unit = {
        this.setParam[Vector3]("position",params,Some(Vector3.zero))
        this.setParam[String]("text",params,Some("Text"))
        this.setParam[Color]("color",params,None)
    }

    override def OnEnter(): Unit = {
        //println("On LabelEnter:"+this.entity.get +"  "+this.entity.get.parent)
    }
}