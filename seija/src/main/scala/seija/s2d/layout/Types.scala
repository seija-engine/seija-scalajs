package seija.s2d.layout

case class Thickness(val left:Float,val top:Float,val right: Float,val bottom:Float) {
  def this(n:Float) = this(n,n,n,n)
  def this(w:Float,h:Float) = this(w,h,w,h)
}

object LayoutAlignment extends Enumeration {
  type LayoutAlignment = Value
  val Start: LayoutAlignment = Value(0)
  val Center: LayoutAlignment = Value(1)
  val End: LayoutAlignment = Value(2)
  val Fill: LayoutAlignment = Value(3)
}