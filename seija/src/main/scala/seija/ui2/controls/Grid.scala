package seija.ui2.controls
import seija.data.Read._
import seija.s2d.layout.{LConst, LNumber, LRate}
import seija.ui2.{Control, ControlCreator}

import scala.scalajs.js
import scala.scalajs.js.Dictionary

class GridCell extends Control {
  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
    setParam[Int]("row",params,Some(0))
    setParam[Int]("col",params,Some(0))
    setParam[Int]("rowSpan",params,Some(0))
    setParam[Int]("colSpan",params,Some(0))
  }
}

class Grid extends Control {
  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
    setLispParam[js.Array[LNumber]]("rows",params,Some(js.Array(LRate(1))))
    setLispParam[js.Array[LNumber]]("cols",params,Some(js.Array(LRate(1))))
  }
}

object GridCell {
  implicit val gridCellCreator:ControlCreator[GridCell] = new ControlCreator[GridCell] {
    override def name: String = "GridCell"
    override def create(): Control = new GridCell
    override def init(): Unit = {}
  }
}

object Grid {
  implicit val gridCreator:ControlCreator[Grid] = new ControlCreator[Grid] {
    override def name: String = "Grid"
    override def create(): Control = new Grid
    override def init(): Unit = {}
  }
}