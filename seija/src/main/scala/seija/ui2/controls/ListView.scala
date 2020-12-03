package seija.ui2.controls
import seija.ui2.{Control}
import scala.scalajs.js.Dictionary
import scala.scalajs.js
import seija.data.XmlNode
import seija.ui2.UITemplate
import seija.core.Transform
import seija.s2d.TextRender
import seija.math.Vector2
import seija.data.CoreRead._
import seija.data.SExpr
import seija.math.Vector3

class ListItem extends Control {

}

class ListView extends Control {
    def childItems:js.Array[Any] = this.property("dataSource").asInstanceOf[js.Array[Any]]
    var itemTemplate:Option[XmlNode] = None
    val Items:js.Array[ListItem] = js.Array();

    override def setParams(params: Dictionary[String]): Unit = {
       this.setParam[Vector3]("position",params,Some(Vector3.zero))
       this.setParam[Vector2]("size",params,Some(Vector2.zero))
       this.setParam[String]("eventRecv",params,None)
       this.setLispParam[js.Array[Any]]("dataSource",params,Some(js.Array()))
    }

    override def setTemplates(tmpls: Dictionary[XmlNode]): Unit = {
       this.itemTemplate = tmpls.get("ItemTemplate")
    }

    override def init(): Unit = {
       super.init()
       val eventRecv = this.property.get("eventRecv");
       if(eventRecv.isDefined && this.eventBoard.isDefined) {
          this.eventBoard.get.register(eventRecv.get.asInstanceOf[String],this.handleEvent);
       }
       println("ListView init:"+this.entity.get)
       this.Items.clear()
       var idx = 0
       for(child <- this.childItems) {
          var itemControl = new ListItem()
          itemControl.dataContent = Some(child)
          itemControl.template = Some(new UITemplate(this.itemTemplate.get,itemControl))
          itemControl.setParent(Some(this))
          itemControl.nsDic = this.nsDic
          itemControl.init()
          itemControl.entity.get.setParent(this.entity)
          this.Items.push(itemControl)
          val trans = itemControl.entity.get.getComponent[Transform]().get;
          trans.localPosition.set(0,50 + idx * -50,0)
          idx += 1
       }
    }

    override def handleEvent(evKey: String, evData: scala.scalajs.js.Array[SExpr]): Unit = {
       evData.head.castKeyword() match {
          case ":Delete" =>
            val deleteIndex = evData(1).caseInt()
            this.Items.remove(deleteIndex).destory()
          case str => 
            println(s"un do $str")
       }
    }

   
}