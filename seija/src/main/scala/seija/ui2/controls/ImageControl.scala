package seija.ui2.controls

import seija.data.{Color, SExpr, SInt, XmlNode}
import seija.math.{Vector2, Vector3}
import seija.ui2.{Control, UITemplate}
import seija.data.CoreRead._

import scala.scalajs.js
import scala.scalajs.js.Dictionary

class ImageControl extends Control {
  override def setParams(params: Dictionary[String]): Unit = {
    this.setParam[Vector3]("position",params,Some(Vector3.zero))
    this.setParam[Vector3]("scale",params,Some(Vector3.one))
    this.setParam[Vector3]("rotation",params,Some(Vector3.zero))
    this.setParam[Vector2]("size",params,Some(Vector2.New(100f,100f)))
    this.setParam[Vector2]("anchor",params,Some(Vector2.New(0.5f,0.5f)))
    this.setParam[Color]("color",params,Some(Color.New(1,1,1,1)))
    this.setParam[Int]("texture",params,None)
    this.setParam[Int]("Int",params,Some(0))
  }

  override def handleEvent(evData: js.Array[SExpr]): Unit = {
    super.handleEvent(evData)
    evData.head.castKeyword() match {
      case ":ClickImage" =>
        val oldInt = this.property("Int").asInstanceOf[Int]
        this.setProperty("Int",oldInt + 1)
        this.emit(":UpdateInt",SInt(oldInt + 1)  )
      case _ => ()
    }
  }
}
