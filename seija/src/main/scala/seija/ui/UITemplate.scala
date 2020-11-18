package seija.ui
import scalajs.js
import seija.core.Entity
import seija.data._

class UITemplate(val parent:SContent) {
  val sContext:SContent = new SContent(Some(parent))

  def handleEvent():Unit = {
  }

  def registerEvent(key:String):Unit = {

  }
}


object UITemplate {
  def create(xmlNode:XmlNode,content: SContent):UITemplate = {
    val newTemplate = new UITemplate(content)
    newTemplate.sContext.set("tmpl",SUserData(newTemplate))

    if(xmlNode.children.isEmpty) {
      return newTemplate
    }
    xmlNode.children.get.head match {
      case node if node.tag == "Entity" =>
        this.parseEntity(node,newTemplate)
      case node =>
    }
    newTemplate
  }

  def parseEntity(xmlNode: XmlNode,tmpl:UITemplate):Entity = {
    val newEntity = Entity.New()
    if(xmlNode.children.isDefined) {
      for(node <- xmlNode.children.get) {
        node.tag match {
          case "Components" =>
            node.children.foreach(arr => {
              for(compNode <- arr) {
                UIComponent.attach(newEntity,compNode,tmpl)
              }
            })
          case nodeTagName =>
        }
      }
    }
    newEntity
  }


  def getXmlNodeParam(xmlNode:XmlNode):js.Dictionary[String] = {
    if(xmlNode.children.isDefined) {
      for(item <- xmlNode.children.get) {
        if(item.tag.startsWith("Param.")) {
          xmlNode.attrs.put(item.tag.drop(6),item.text.getOrElse(""))
        }
      }
    }
    xmlNode.attrs
  }

  def initParam[T](name:String,dic:js.Dictionary[String],setFunc:(T) => Unit,content: SContent)(implicit readT:Read[T]):Unit = {
    dic.get(name).map(Utils.parseParam).foreach {
      case Left(value) => readT.read(value).foreach(setFunc)
      case Right(value) =>
        SExprInterp.eval(value, Some(content)) match {
          case SUserData(value) =>
            setFunc(value.asInstanceOf[T])
          case _ => ()
        }

    }
  }

}