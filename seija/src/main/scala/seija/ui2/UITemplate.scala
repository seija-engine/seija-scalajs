package seija.ui2
import scala.scalajs.js
import seija.core.Entity
import seija.data.XmlNode
import seija.math.Vector2
import seija.data.XmlExt.RichXmlNode

class UITemplate(val xmlNode: XmlNode,val control: Control) {
  def create():Either[String,Entity]  = {
    if(xmlNode.children.isEmpty || xmlNode.children.get.length == 0) {
      return Left("template need children")
    }
    val firstNode = xmlNode.children.get(0)
    this.parse(firstNode,None)
  }

  def parse(xmlNode: XmlNode,parent:Option[Entity]):Either[String,Entity] = {
    xmlNode.tag match {
      case "Entity" => this.parseEntity(xmlNode,parent)
      case _ => this.parseControl(xmlNode,parent)
    }
  }

  def parseEntity(xmlNode: XmlNode,parent:Option[Entity]) :Either[String,Entity] = {
    val newEntity = Entity.New()
    newEntity.setParent(parent)
    if(xmlNode.children.isDefined) {
      for(node <- xmlNode.children.get) {
        node.tag match {
          case "Components" =>
            node.children.foreach(arr => {
              for(compNode <- arr) {
                 UISystem.getUIComp(compNode.tag).foreach(_.attach(newEntity,compNode,this))
              }
            })
          case _ => this.parse(node,Some(newEntity)).left.foreach(println)
          
        }
      }
    }
    Right(newEntity)
  }

  def parseControl(xmlNode: XmlNode,parent:Option[Entity]):Either[String,Entity] = {
    val arrName = xmlNode.tag.split(':')
    val pathHead = if(arrName.length > 0) {
      val nsPath = this.control.nsDic.get(arrName(0))
      if(nsPath.isEmpty) {
        throw new Exception(s"${arrName(0)} not found ns path")
      }
      nsPath.get
    } else ""
    val controlPath = pathHead + arrName(1) + ".xml"
    val dic = Utils.getXmlNodeParam(xmlNode)
    val childs = xmlNode.children.getOrElse(js.Array())
    val newControl = UISystem.create(controlPath,dic,Some(this.control),childs)
    newControl match {
      case Left(value) => Left(value)
      case Right(control) =>
        control.entity.get.setParent(parent)
        Right(control.entity.get)
    }
  }
}
