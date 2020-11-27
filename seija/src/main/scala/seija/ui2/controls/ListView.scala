package seija.ui2.controls
import seija.ui2.{Control}
import scala.scalajs.js.Dictionary
import scala.scalajs.js
import seija.data.XmlNode
import seija.ui2.UITemplate
import seija.core.Transform

class ListItem extends Control {

}

class ListView extends Control {
    def childItems:js.Array[Any] = this.property("dataSource").asInstanceOf[js.Array[Any]]
    var itemTemplate:Option[XmlNode] = None

    override def setParams(params: Dictionary[String]): Unit = {
       this.setLispParam[js.Array[Any]]("dataSource",params,Some(js.Array()))  
    }

    override def setTemplates(tmpls: Dictionary[XmlNode]): Unit = {
       this.itemTemplate = tmpls.get("ItemTemplate")
    }

    override def init(): Unit = {
       super.init()
       println("ListView init")
       var idx = 0
       for(child <- this.childItems) {
          var itemControl = new ListItem()
          itemControl.dataContent = Some(child)
          itemControl.template = Some(new UITemplate(this.itemTemplate.get,itemControl))
          itemControl.setParent(Some(this))
          itemControl.init()
          
         
          val trans = itemControl.entity.get.getComponent[Transform]().get;
          trans.localPosition.set(0,50 + idx * -50,0)
          
          idx += 1
       }
    }
}