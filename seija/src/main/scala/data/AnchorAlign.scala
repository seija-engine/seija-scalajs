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

  implicit val anchorAlignRead: data.Read[AnchorAlign] = {
    case "TopLeft" => Some(AnchorAlign.TopLeft)
    case "Top" => Some(AnchorAlign.Top)
    case "TopRight" => Some(AnchorAlign.TopRight)
    case "Left" => Some(AnchorAlign.Left)
    case "Center" => Some(AnchorAlign.Center)
    case "Right" => Some(AnchorAlign.Right)
    case "BottomLeft" => Some(AnchorAlign.BottomLeft)
    case "Bottom" => Some(AnchorAlign.Bottom)
    case "BottomRight" => Some(AnchorAlign.BottomRight)
    case _ => None
  }
}