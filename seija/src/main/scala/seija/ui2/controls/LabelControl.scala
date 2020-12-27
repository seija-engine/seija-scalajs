package seija.ui2.controls

import seija.ui2.Control
import scala.scalajs.js.Dictionary
import seija.data.CoreRead._
import seija.math.Vector3
import seija.data.Color

class LabelControl extends Control {
    override def setParams(params: Dictionary[String]): Unit = {
        PropertySet.setLayout(this,params)
        this.setParam[Vector3]("position",params,Some(Vector3.zero))
        this.setParam[String]("text",params,Some("Text"))
        this.setParam[Color]("color",params,None)
        this.setParam[Int]("fontSize",params,Some(16))

        this.setEventParam("OnClick",params)
    }

    override def OnEnter(): Unit = {
        //println("On LabelEnter:"+this.entity.get +"  "+this.entity.get.parent)
    }
}