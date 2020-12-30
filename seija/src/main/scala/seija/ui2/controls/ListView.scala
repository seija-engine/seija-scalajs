package seija.ui2.controls
import demo.TDemoItem
import seija.ui2.{Control, ControlCreator, UITemplate}

import scala.scalajs.js.Dictionary
import scala.scalajs.js
import seija.data.XmlNode
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

    override def setTemplates(temples: Dictionary[XmlNode]): Unit = {
       super.setTemplates(temples)
       this.itemTemplate = temples.get("ItemTemplate")
    }

    override def init(): Unit = {
       super.init()
       val eventRecv = this.property.get("eventRecv");
       if(eventRecv.isDefined && this.eventBoard.isDefined) {
          this.eventBoard.get.register(eventRecv.get.asInstanceOf[String],this.handleEvent);
       }
       println("ListView init:"+this.entity.get)
       this.Items.clear()
       for(child <- this.childItems) {
          val itemControl = createItem(child)
          this.Items.push(itemControl)
       }
      this.testLayout()
    }

    def testLayout():Unit = {
      for(index <- 0 until this.Items.length) {
        val trans = this.Items(index).entity.get.getComponent[Transform]().get;
        trans.localPosition.set(0, 50 + index * -50, 0)
      }
    }

    def createItem(itemData:Any):ListItem = {
      val itemControl = new ListItem()
      itemControl.dataContent = Some(itemData)
      itemControl.template = Some(new UITemplate(this.itemTemplate.get,itemControl))
      itemControl.setParent(Some(this))
      itemControl.nsDic = this.nsDic
      itemControl.init()
      itemControl.entity.get.setParent(this.entity)
      itemControl
    }

    override def handleEvent(evKey: String, evData: scala.scalajs.js.Array[SExpr]): Unit = {
       evData.head.castKeyword() match {
          case ":Delete" =>
            val deleteIndex = evData(1).caseInt()
            this.Items.remove(deleteIndex).destroy()
            this.testLayout()
          case ":Add" =>
            val addItem = evData(1).toValue[Any]
            val newItem = this.createItem(addItem)
            this.Items.push(newItem)
            this.testLayout()
          case ":Check" =>
            val index = evData(1).caseInt()
            val childs = this.Items(index).entity.get.children
            for(c <- childs) {
              val textRender = c.getComponent[TextRender]()
              if(textRender.isDefined && textRender.get.text =="Q") {
                println("set NMB")
                textRender.get.setText("NMB")
              }
            }
          case str => 
            println(s"un do $str")
       }
    }

   
}

object ListView {
  implicit val listViewCreator:ControlCreator[ListView] = new ControlCreator[ListView] {
    override def name: String = "ListView"
    override def create(): Control = new ListView
    override def init(): Unit = {}
  }
}