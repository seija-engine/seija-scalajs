package seija.ui.controls
import scala.scalajs.js
import seija.ui.Control
import seija.ui.ControlParams
import seija.data.Read
import MenuItem._
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.layout.ContentView
import seija.ui.comps.LayoutViewComp
import seija.data.SExpr
import seija.data.SInt

case class MenuItemData(name:String,children:js.Array[MenuItemData])

object MenuItem {
    implicit val readMenuItemData:Read[js.Array[MenuItemData]] = (string: String) => None
}

class MenuItem extends Control with LayoutViewComp {

    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        this.slots.put("Children",this)
        this.initProperty[String]("Name",params.paramStrings,Some("MenuItem"),None)
        this.initProperty[js.Array[MenuItemData]]("Children",params.paramStrings,Some(js.Array()),None)
        this.initProperty[Boolean]("IsSelect",params.paramStrings,Some(false),None)
        this.initProperty[Int]("Index",params.paramStrings,Some(0),None)

        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        val view = entity.addComponent[ContentView]()
        initLayoutView(this,view,params)
    }

    def isSelect:Boolean = this.property("IsSelect").asInstanceOf[Boolean]
    def isSelect_=(newValue:Boolean) = this.setProperty("IsSelect",newValue)
    def Index:Int = this.property("Index").asInstanceOf[Int]

    def setName(name:String) {
        this.setProperty("Name",name)
    }

    def setChildren(children:js.Array[MenuItemData]) {
        this.setProperty("Children",children)
    }

    def setIndex(index:Int) {
        this.setProperty("Index",index)
    }

    override def handleEvent(evKey: String, evData: js.Array[SExpr]): Unit = {
      evKey match {
          case ":select-menu" | ":select-menu-enter" =>
            this.parent.foreach(_.handleEvent(evKey,js.Array(SInt(this.Index))))
          case _ => this.parent.foreach(_.handleEvent(evKey,evData))
      }
    }
}