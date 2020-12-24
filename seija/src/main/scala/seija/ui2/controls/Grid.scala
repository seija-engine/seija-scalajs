package seija.ui2.controls
import seija.s2d.layout.{LConst, LNumber, LRate}
import seija.ui2.Control

import scala.scalajs.js
import scala.scalajs.js.Dictionary

class GridCell extends Control {
  
}

class Grid extends Control {
  override def setParams(params: Dictionary[String]): Unit = {
    PropertySet.setLayout(this,params)
    setLispParam[js.Array[LNumber]]("rows",params,Some(js.Array(LRate(1))))
    setLispParam[js.Array[LNumber]]("cols",params,Some(js.Array(LRate(1))))
  }
}