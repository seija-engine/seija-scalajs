package seija.ui.controls

import seija.ui.Control
import seija.ui.ControlCreator
import seija.ui.ControlParams
import seija.core.Entity
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView
import seija.ui.comps.LayoutViewComp
import seija.data.XmlExt._
import scalajs.js
import seija.ui.SExprContent
import seija.data._
import seija.ui.controls.MenuItem._
import seija.data.SUserData
import slogging.LazyLogging
import seija.ui.UITemplate
import seija.core.event.EventSystem
import seija.core.event.GameEventType
import seija.core.event.GameEvent
import seija.core.Time
import seija.ui.UISystem
import seija.core.event.CABEventRoot
import seija.math.Vector3

object Menu {
    implicit val menuCreator:ControlCreator[Menu] = new ControlCreator[Menu] {
        val name: String = "Menu"
        def init(): Unit = {
            SExprContent.content.set("menu-item",SNFunc(menuItem))
        }
        def create(): Menu = new Menu
    }
    

    def menuItem(args:js.Array[SExpr],content:SContent):SExpr = {
        val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
        val menuName = evalArgs(0).castString()
        val child:js.Array[MenuItemData] = if(evalArgs.length > 1) {
            evalArgs(1).toValue[js.Array[MenuItemData]]
        } else { js.Array() }
        SUserData(MenuItemData(menuName,child))
    }
}

class Menu extends Control with LayoutViewComp with LazyLogging {
    var itemTemplate:Option[XmlNode] = None
    var menuDatas:js.Array[MenuItemData] = js.Array()
    var menuItems:js.Array[MenuItem] = js.Array()
    var selectIndex:Int = -1
    var isSelecting:Boolean = false
    var globalIdxRef:Option[IndexedRef] = None
    var notGlobalCloseFrame:Int = -1
    var contextMenu:Option[ContextMenu] = None

    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        this.itemTemplate = params.paramXmls.get("ItemTemplate")
        
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        val contentView = entity.addComponent[ContentView]()
        initLayoutView(this,contentView,params)
        
        initProperty[js.Array[MenuItemData]]("dataSource",params.paramStrings,None,None)
    }

    override def OnEnter(): Unit = {
       val dataSource = this.property.get("dataSource")
       if(dataSource.isDefined) {
           onSetDataSource(dataSource.get.asInstanceOf[js.Array[MenuItemData]])
       } 
    }

    def onSetDataSource(data:js.Array[MenuItemData]) {
        this.menuDatas = data
        this.menuItems.foreach(_.destroy())
        this.menuItems.clear()
        if(itemTemplate.isEmpty) return
        val template = this.itemTemplate.get
        var idx = 0
        for(child <- this.menuDatas) {
            val menuItem = new MenuItem()
            menuItem.init(this.slots.get("Children"),
                         ControlParams(paramXmls = js.Dictionary("Template" -> this.itemTemplate.get)),
                         Some(menuItem))
            menuItem.setName(child.name)
            menuItem.setChildren(child.children)
            menuItem.setIndex(idx)
            this.menuItems.push(menuItem)
            idx += 1
        }
    }

    def selectItem(index:Int) {
        if(this.selectIndex == index) return
        this.selectIndex = index
        for(idx <- 0 to this.menuItems.length - 1) {
            val item = this.menuItems(idx)
            if(this.selectIndex == idx) {
                item.isSelect = true
            } else {
                item.isSelect = false
            }
        }
        val selectItem = this.menuItems(this.selectIndex)
        if(contextMenu.isDefined) {
            this.contextMenu.get.setParent(None)
        } else {
            UISystem.createByFile("/core/ContextMenu.xml",None,ControlParams(),None) match {
            case Left(errString) => logger.error(errString)
            case Right(contextMenu) =>
              contextMenu.entity.get.addComponent[CABEventRoot]()
              this.contextMenu = Some(contextMenu.asInstanceOf[ContextMenu])
            }
        }
        val view = this.contextMenu.get.entity.get.getComponent[ContentView]();

        val sizeX = selectItem.entity.get.getComponent[Rect2D]().get.size.x
        view.get.setPosition(Vector3.New(this.selectIndex * sizeX,0,0))
        this.contextMenu.get.setProperty("dataSource",this.menuDatas(this.selectIndex).children)
    }

    def unSelectAll() {
        this.selectIndex = -1
        this.menuItems.foreach(_.isSelect = false)
        if(this.contextMenu.isDefined) {
            this.contextMenu.get.destroy()
            this.contextMenu = None
        }
    }

    override def handleEvent(evKey: String, evData: js.Array[SExpr]): Unit = {
        evKey match {
            case ":select-menu" =>
                if(this.notGlobalCloseFrame == Time.frame()) return
                if(this.isSelecting) {
                    this.unSelectAll()
                    return
                }
                this.isSelecting = true
                val index = evData(0).caseInt()
                this.selectItem(index)
                this.notGlobalCloseFrame = Time.frame()
                this.globalIdxRef = EventSystem.addGlobalEvent(GameEventType.TouchStart,this.onGlobalTouch)
                
            case ":select-menu-enter" =>
                val index = evData(0).caseInt()
                if(this.isSelecting) {
                    this.selectItem(index)
                }
            case _ => this.parent.foreach(_.handleEvent(evKey,evData))
        }
    }

    def onGlobalTouch(ev:GameEvent) {
        if(this.notGlobalCloseFrame == Time.frame()) return
        this.notGlobalCloseFrame = Time.frame()
        this.isSelecting = false
        this.unSelectAll()
        this.globalIdxRef.foreach(idxRef => {
            EventSystem.removeGlobalEvent(GameEventType.TouchStart,idxRef)
        });
    }
}