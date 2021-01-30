package seija.ui.controls
import seija.ui.ControlCreator
import seija.ui.Control
import seija.ui.ControlParams
import seija.ui.comps.LayoutViewComp
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView
import seija.data.SExpr
import seija.ui.UISystem
import seija.s2d.layout.Thickness
import scala.scalajs.js
import seija.data.IndexedRef
import seija.core.event.EventSystem
import seija.core.event.GameEventType
import seija.core.event.GameEvent
import seija.core.Time
import seija.ui.controls.MenuItem._

object SelectBox {
    implicit val selectBoxCreator:ControlCreator[SelectBox] = new ControlCreator[SelectBox] {
        val name: String = "SelectBox"
        def init(): Unit = {}
        def create(): SelectBox = new SelectBox
    }
}

class SelectBox extends Control with LayoutViewComp {
    var globalIdxRef:Option[IndexedRef] = None
    var contextMenu:Option[ContextMenu] = None
    
    var createFrame:Int = -1
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        val view = entity.addComponent[ContentView]()

        initLayoutView(this,view,params);
        initProperty[js.Array[MenuItemData]]("dataSource",params.paramStrings,None,None)

        val dataList = this.getProperty[js.Array[MenuItemData]]("dataSource").getOrElse(js.Array());
        val firstValue = if(dataList.length > 0) {
            dataList(0).name
        } else {
            ""
        }
        initProperty[String]("curValue",params.paramStrings,Some(firstValue),None);
    }


    override def handleEvent(evKey: String, evData: scala.scalajs.js.Array[SExpr]): Unit = {
        logger.info(evKey)
       
        evKey match {
            case ":click-box" =>
                this.createMenu()
            case _ =>
        }
    }

    def createMenu() {
        if(this.contextMenu.isDefined) return;
        this.createFrame = Time.frame();
        val menuLayer = this.layerName + "Menu";
        val params = ControlParams();
        params.paramStrings.put("layer",menuLayer)
        val width = this.getProperty[Float]("width").getOrElse(100f);
        params.paramStrings.put("width",width.toString());
        UISystem.createByFile("/core/ContextMenu.xml",None,params,None) match {
            case Left(errString) => logger.error(errString)
            case Right(control) => 
                val contextMenu = control.asInstanceOf[ContextMenu];
                contextMenu.OnSelectMenu = Some(this.OnSelectContextMenu)
                this.contextMenu = Some(contextMenu)
                control.getView.get.setMargin(Thickness(252,213,0,0))
                control.setProperty("dataSource",this.getProperty("dataSource").getOrElse(js.Array()))
        }
        this.globalIdxRef = EventSystem.addGlobalEvent(GameEventType.TouchStart,this.onGlobalTouch)
    }

    def onGlobalTouch(ev:GameEvent) {
        if(this.createFrame == Time.frame()) return;
       
        this.contextMenu.foreach(_.destroy())
        this.globalIdxRef.foreach(idxRef => {
            EventSystem.removeGlobalEvent(GameEventType.TouchStart,idxRef)
        });
        this.contextMenu = None;
    }

    def OnSelectContextMenu(index:Int,key:String) {
        this.setProperty("curValue",key)
        logger.info(key)
    }
}