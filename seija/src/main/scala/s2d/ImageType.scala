package s2d

import core.ToJSValue

import scala.scalajs.js

sealed trait ImageType extends ToJSValue
case object ImageSimple extends ImageType {
    override def toJsValue: js.Any = 0
}
case class  ImageSliced(left:Float,right:Float,top:Float,bottom:Float) extends ImageType {
    override def toJsValue: js.Any = js.Array(1,left,right,top,bottom)
}
case class  ImageFilled(fillType:ImageFilledType.ImageFilledType,value:Float) extends ImageType {
    override def toJsValue: js.Any = js.Array(2,fillType.id,value)
}
case object ImageTiled extends ImageType {
    override def toJsValue: js.Any = 3
}

object ImageType {
    implicit val imageTypeRead: data.Read[ImageType] = (string: String) => {
        None
    }
}


object ImageFilledType extends Enumeration {
    type ImageFilledType = Value;
    val HorizontalLeft: ImageFilledType = Value(0);
    val HorizontalRight: ImageFilledType = Value(1);
    val VerticalTop: ImageFilledType = Value(2);
    val VerticalBottom: ImageFilledType = Value(3);
}