package seija.s2d.layout

import seija.core.{BaseComponent, Component, Entity, Foreign}
class GridCell (override val entity:Entity) extends BaseComponent(entity) {
    protected var _row:Int = 0
    protected var _col:Int = 0
    protected var _rowSpan:Int = 0
    protected var _colSpan:Int = 0
    def row:Int = _row
    def col:Int = _col
    def rowSpan:Int = _row
    def colSpan:Int = _col

    def setRow(row:Int):Unit = {
        _row = row
        updateToRust()
    }

    def setCol(col:Int):Unit = {
        _col = col
        updateToRust()
    }

    def setRowSpan(rowSpan:Int):Unit = {
        _rowSpan = rowSpan
        println(s"rownSppp ${_rowSpan}")
        updateToRust()
    }

    def setColSpan(colSpan:Int):Unit = {
        _colSpan = colSpan
        updateToRust()
    }

    protected def updateToRust():Unit = Foreign.setGridCell(this.entity.id,_row,_col,_rowSpan,_colSpan)
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