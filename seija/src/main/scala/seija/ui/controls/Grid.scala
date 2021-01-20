package seija.ui.controls
import scala.scalajs.js
import seija.ui.Control
import seija.ui.ControlParams
import seija.core.Entity
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.layout
import seija.s2d.layout.GridLayout
import seija.ui.comps.LayoutViewComp
import seija.ui.ControlCreator
import seija.s2d.layout.ContentView
import seija.s2d.layout.LNumber
import seija.data.Read
import seija.s2d.layout.LRate

object Grid {
    implicit val gridCreator:ControlCreator[Grid] = new ControlCreator[Grid] {
        val name: String = "Grid"
        def init(): Unit = {}
        def create(): Grid = new Grid
    }

    
}

class Grid extends Control with LayoutViewComp {
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        val gridLayout = entity.addComponent[GridLayout]()
        this._view = Some(gridLayout)
        initLayoutView(this,gridLayout,params)
        initProperty[js.Array[LNumber]]("rows",params.paramStrings,Some(js.Array(LRate(1))),Some((rows) => {
            gridLayout.setRows(rows)
        }))
        initProperty[js.Array[LNumber]]("cols",params.paramStrings,Some(js.Array(LRate(1))),Some((cols) => {
            gridLayout.setCols(cols)
        }))
    }
}

object GridCell {
    implicit val gridCellCreator:ControlCreator[GridCell] = new ControlCreator[GridCell] {
        val name: String = "GridCell"
        def init(): Unit = {}
        def create(): GridCell = new GridCell
    }
}

class GridCell extends Control with LayoutViewComp {
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        this.slots.put("Children",this)
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        val contentView = entity.addComponent[ContentView]()
        val gridCell = entity.addComponent[layout.GridCell]()
        this._view = Some(contentView)
        this.initLayoutView(this,contentView,params)
        this.initProperty[Int]("row",params.paramStrings,Some(0),Some((row) => gridCell.setRow(row)))
        this.initProperty[Int]("col",params.paramStrings,Some(1),Some((col) => gridCell.setCol(col)))
        this.initProperty("rowSpan",params.paramStrings,Some(0),Some((rowSpan) => gridCell.setRowSpan(rowSpan)))
        this.initProperty("colSpan",params.paramStrings,Some(0),Some((colSpan) => gridCell.setColSpan(colSpan)))

        
    }
}