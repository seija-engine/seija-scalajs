package seija.s2d.layout

import seija.core.{BaseComponent, Component, Entity, Foreign}

import scala.scalajs.js

class GridLayout(override val entity:Entity) extends LayoutView(entity) {
  protected var _rows:js.Array[LNumber] = js.Array()
  protected var _cols:js.Array[LNumber] = js.Array()

  def rows:js.Array[LNumber] = _rows
  def cols:js.Array[LNumber] = _cols

  def addRow(num:LNumber):Unit = {
    _rows.push(num)
    Foreign.addGridRow(this.entity.id,num.typ(),num.value())
  }

  def setRows(rows:js.Array[LNumber]):Unit = {
    _rows.clear()
    _rows = rows
    Foreign.setGridRows(this.entity.id,LNumber.arrToJs(rows))
  }

  def addCol(num:LNumber):Unit = {
    _cols.push(num);
    Foreign.addGridCol(this.entity.id,num.typ(),num.value())
  }

  def setCols(cols:js.Array[LNumber]):Unit = {
    _cols.clear()
    _cols = cols
    Foreign.setGridCols(this.entity.id,LNumber.arrToJs(cols))
  }
}

object GridLayout {
  implicit val gridLayoutComp:Component[GridLayout] = new Component[GridLayout] {
    override def addToEntity(e: Entity): GridLayout = {
      Foreign.addGridLayout(e.id)
      new GridLayout(e)
    }

    override val key: String = "GridLayout"
  }
}
