package seija.ui.controls
import scala.scalajs.js
import seija.ui.Control
import seija.ui.ControlParams
import seija.ui.controls.MenuItem._
import seija.ui.ControlCreator
import seija.core.Transform
import seija.s2d.Rect2D
import seija.data.XmlNode
import seija.s2d.layout.ContentView
import seija.ui.comps.LayoutViewComp
import seija.data.SExpr
import seija.ui.comps.EventNodeComp
import seija.ui.UISystem

object ContextMenu {
  implicit val imageCreator:ControlCreator[ContextMenu] = new ControlCreator[ContextMenu] {
        val name: String = "ContextMenu"
        def init(): Unit = {}
        def create(): ContextMenu = new ContextMenu
  }
}

class ContextMenu extends Control with LayoutViewComp with EventNodeComp {
    var menuDatas:js.Array[MenuItemData] = js.Array()
    var menuItems:js.Array[MenuItem] = js.Array()
    var itemTemplate:Option[XmlNode] = None
    

    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        this.itemTemplate = params.paramXmls.get("ItemTemplate")
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        this._view = Some(entity.addComponent[ContentView]())
        initLayoutView(this,this._view.get,params)
        initEventComp(this,params)
        initProperty[js.Array[MenuItemData]]("dataSource",params.paramStrings,None,Some(onSetDataSource))
    }

    override def handleEvent(evKey: String, evData: scala.scalajs.js.Array[SExpr]): Unit = {
      evKey match {
        case ":select-menu" | ":select-menu-enter" =>
          val index = evData(0).caseInt()
          UISystem.createByFile("sled/SelectFile.xml",None,ControlParams(),None)
          println("create:"+index)
        case _ => ()
      }
    }

    def onSetDataSource(menuItems:js.Array[MenuItemData]) {
      this.menuDatas = menuItems
      this.menuItems.foreach(_.destroy())
      this.menuItems.clear()
      if(itemTemplate.isEmpty) return
      var idx = 0
      for(child <- this.menuDatas) {
        
        val menuItem = new MenuItem()
        menuItem.init(this.slots.get("Children"),
                         ControlParams(paramXmls = js.Dictionary("Template" -> this.itemTemplate.get)),
                         Some(this))
        menuItem.setName(child.name)
        menuItem.setChildren(child.children)
        menuItem.setIndex(idx)
        this.menuItems.push(menuItem)
        idx += 1
      }
    }

    
}