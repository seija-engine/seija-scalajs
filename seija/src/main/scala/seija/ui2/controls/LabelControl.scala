package seija.ui2.controls

import seija.ui2.{Control, ControlCreator}

import scala.scalajs.js.Dictionary
import seija.data.CoreRead._
import seija.math.Vector3
import seija.data.Color

class LabelControl extends Control {
    override def setParams(params: Dictionary[String]): Unit = {
        PropertySet.setLayout(this,params)
        this.setParam[Vector3]("position",params,Some(Vector3.zero))
        this.setParam[String]("text",params,Some("Text"))
        this.setParam[Color]("color",params,Some(Color.New(0,0,0,1)))
        this.setParam[Int]("fontSize",params,Some(16))
        this.setEventParam("OnClick",params)
        this.setEventParam("OnMouseEnter",params)
        this.setEventParam("OnTouchStart",params)
    }
}

object LabelControl {
    implicit val LabelCreator:ControlCreator[LabelControl] = new ControlCreator[LabelControl] {
        override def name: String = "LabelControl"
        override def create(): Control = new LabelControl
        override def init(): Unit = {}
    }
}