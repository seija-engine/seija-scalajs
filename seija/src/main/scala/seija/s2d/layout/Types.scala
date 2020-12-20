package seija.s2d.layout

import seija.data.Read

case class Thickness(val left:Float,val top:Float,val right: Float,val bottom:Float) {
  def this(n:Float) = this(n,n,n,n)
  def this(w:Float,h:Float) = this(w,h,w,h)
}
object Thickness {
  implicit val readThickness:Read[Thickness] = new Read[Thickness] {
    override def read(string: String): Option[Thickness] = {
      val numArr = string.split(",")
      numArr.length match {
        case 1 =>
          numArr(0).toFloatOption.map(n => new Thickness(n))
        case 2 =>
          for {
             w <- numArr(0).toFloatOption
             h <- numArr(1).toFloatOption
          } yield new Thickness(w,h)
        case 4 =>
          for {
            l <- numArr(0).toFloatOption
            t <- numArr(1).toFloatOption
            r <- numArr(2).toFloatOption
            b <- numArr(3).toFloatOption
          } yield new Thickness(l,t,r,b)
        case _ => None
      }
    }
  }
}

object LayoutAlignment extends Enumeration {
  type LayoutAlignment = Value
  val Start: LayoutAlignment = Value(0)
  val Center: LayoutAlignment = Value(1)
  val End: LayoutAlignment = Value(2)
  val Fill: LayoutAlignment = Value(3)

  implicit val layoutAlignmentRead:Read[LayoutAlignment] = new Read[LayoutAlignment] {
    override def read(string: String): Option[LayoutAlignment] = {
      string match {
        case "Start" => Some(LayoutAlignment.Start)
        case "Center" => Some(LayoutAlignment.Center)
        case "End" => Some(LayoutAlignment.End)
        case "Fill" => Some(LayoutAlignment.Fill)
        case _ => None
      }
    }
  }
}

object  Orientation extends Enumeration {
  type Orientation = Value
  val Horizontal:Orientation = Value(0)
  val Vertical:Orientation = Value(1)

  implicit val readOrientation:Read[Orientation] = new Read[Orientation] {
    override def read(string: String): Option[Orientation] = {
      string match {
        case "Horizontal" => Some(Orientation.Horizontal)
        case "Vertical" => Some(Orientation.Vertical)
        case _ => None
      }
    }
  }
}

sealed trait LNumber {
  def typ():Int =  0
  def value():Float
}
case class LConst(number:Float) extends LNumber {
  override def value(): Float = number
}
case class LRate(number:Float) extends LNumber {
  override def typ(): Int = 1

  override def value(): Float = number
}