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
    implicit val imageTypeRead: data.Read[ImageType] = {
        case "Simple" => Some(ImageSimple)
        case "Tiled" => Some(ImageTiled)
        case str if str.startsWith("Sliced(") =>
            val arr = str.slice(7,str.length - 1).split(',')
            if(arr.length == 4) {
                for {
                    l <- arr(0).toFloatOption
                    r <- arr(1).toFloatOption
                    t <- arr(2).toFloatOption
                    b <- arr(3).toFloatOption
                } yield ImageSliced(l,r,t,b)
            } else {
                None
            }
        case str if str.startsWith("Filled(") =>
            val arr = str.slice(7,str.length -1).split(',')
            if(arr.length == 2) {
                for {
                   ft <- ImageFilledType.filledTypeRead.read(arr(0)) 
                   value <- arr(1).toFloatOption
                } yield ImageFilled(ft,value)
            } else {
                None
            }   
    }
}


object ImageFilledType extends Enumeration {
    type ImageFilledType = Value;
    val HorizontalLeft: ImageFilledType = Value(0);
    val HorizontalRight: ImageFilledType = Value(1);
    val VerticalTop: ImageFilledType = Value(2);
    val VerticalBottom: ImageFilledType = Value(3);

     implicit val filledTypeRead: data.Read[ImageFilledType] = {
        case "HorizontalLeft" => Some(ImageFilledType.HorizontalLeft)
        case "HorizontalRight" => Some(ImageFilledType.HorizontalRight)
        case "VerticalTop" => Some(ImageFilledType.VerticalTop)
        case "VerticalBottom" => Some(ImageFilledType.VerticalBottom)
        case _ => None
  }
}