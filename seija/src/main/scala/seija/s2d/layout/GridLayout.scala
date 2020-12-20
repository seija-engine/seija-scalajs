package seija.s2d.layout

import seija.core.{BaseComponent, Component, Entity, Foreign}

import scala.scalajs.js

class GridCell(override val entity: Entity) extends BaseComponent(entity) {
  protected var _row:Int = 0
  protected var _col:Int = 0
  protected var _rowSpan:Int = 0
  protected var _colSpan:Int = 0

  def set(row:Int,col:Int,rowSpan:Int,colSpan:Int):Unit = {
    _row = row
    _col = col
    _rowSpan = rowSpan
    _colSpan = colSpan
    updateCell()
  }

  protected def updateCell():Unit = {
    Foreign.setGridCell(this.entity.id,_row,_col,_rowSpan,_colSpan)
  }
}

object GridCell {
  implicit val gridCellComp:Component[GridCell] = new Component[GridCell] {
    override def addToEntity(e: Entity): GridCell = {
      Foreign.addGridCell(e.id)
      new GridCell(e)
    }
    override val key: String = "GridCell"
  }
}

class GridLayout(override val entity:Entity) extends LayoutView(entity) {
  protected var _rows:js.Array[LNumber] = js.Array()
  protected var _cols:js.Array[LNumber] = js.Array()

  def rows:js.Array[LNumber] = _rows
  def cols:js.Array[LNumber] = _cols

  def addRow(num:LNumber):Unit = {
    _rows.push(num)
    Foreign.addGridRow(this.entity.id,num.typ(),num.value())
  }

  def addCol(num:LNumber):Unit = {
    _cols.push(num);
    Foreign.addGridCol(this.entity.id,num.typ(),num.value())
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
