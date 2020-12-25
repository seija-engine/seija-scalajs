package seija.ui2.controls
import seija.data.CoreRead._
import seija.s2d.layout.{LConst, LNumber, LRate}
import seija.ui2.Control

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