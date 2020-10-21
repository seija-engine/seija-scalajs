package s2d

sealed trait ImageType
case object ImageSimple extends ImageType
case class  ImageSliced(left:Float,right:Float,top:Float,bottom:Float) extends ImageType
case class  ImageFilled(fillType:ImageFilledType.ImageFilledType,value:Float) extends ImageType
case object ImageTiled extends ImageType


object ImageFilledType extends Enumeration {
    type ImageFilledType = Value;
    val HorizontalLeft = Value(0);
    val HorizontalRight = Value(1);
    val VerticalTop = Value(2);
    val VerticalBottom = Value(3);
}