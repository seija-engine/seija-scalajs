package seija.ui2.controls

import seija.data.Color
import seija.math.{Vector2, Vector3}
import seija.ui2.{Control, ControlCreator}
import seija.data.Read._
import seija.s2d.ImageType

import scala.scalajs.js.Dictionary

class SpriteControl extends Control {
  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
    this.setParam[Vector3]("position",params,Some(Vector3.zero) )
    this.setParam[Vector3]("scale",params,Some(Vector3.one) )
    this.setParam[Vector3]("rotation",params,Some(Vector3.zero))
    this.setParam[Vector2]("size",params,Some(Vector2.New(100f,100f)))
    this.setParam[Vector2]("anchor",params,Some(Vector2.New(0.5f,0.5f)))
    this.setParam[Color]("color",params,Some(Color.New(1,1,1,1)))
    this.setParam[String]("spriteName",params,None)
    this.setParam[Int]("sheet",params,None)
    this.setParam[ImageType]("type",params,None)

    this.setEventParam("OnClick",params)
  }
}

object SpriteControl {
  implicit val spriteCreator:ControlCreator[SpriteControl] = new ControlCreator[SpriteControl] {
    override def name: String = "SpriteControl"
    override def create(): Control = new SpriteControl
    override def init(): Unit = {}
  }
}
