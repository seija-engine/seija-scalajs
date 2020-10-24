package data

object AnchorAlign  extends Enumeration {
  type AnchorAlign = Value;
  val TopLeft:AnchorAlign = Value(0)
  val Top:AnchorAlign = Value(1)
  val TopRight:AnchorAlign = Value(2)
  val Left:AnchorAlign = Value(3)
  val Center:AnchorAlign = Value(4)
  val Right:AnchorAlign = Value(5)
  val BottomLeft:AnchorAlign = Value(6)
  val Bottom:AnchorAlign = Value(7)
  val BottomRight:AnchorAlign = Value(8)
}