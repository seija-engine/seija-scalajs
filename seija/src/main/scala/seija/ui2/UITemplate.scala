package seija.ui2
import scala.scalajs.js
import seija.core.Entity
import seija.data.XmlNode
import seija.math.Vector2
import seija.data.XmlExt.RichXmlNode
import slogging.LazyLogging

class UITemplate(val xmlNode: XmlNode,val control: Control,val slots:js.Dictionary[Entity] = js.Dictionary()) extends LazyLogging {
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
                UISystem.getUIComp(compNode.tag) match {
                  case Some(value) => value.attach(newEntity,compNode,this.control)
                  case None => logger.error(s"not found ${compNode.tag} UIComponent")
                }
              }
            })
          case str if str.startsWith("Slot.") =>
            this.slots.put(str.substring("Slot".length),newEntity)
          case _ =>
            this.parse(node,Some(newEntity)).left.foreach(println)
        }
      }
    }
    
    Right(newEntity)
  }
  def getFullPath(xmlNode: XmlNode): String = {
    val arrName = xmlNode.tag.split(':')
    if(arrName.length > 1) {
      val nsPath = this.control.nsDic.get(arrName(0))
      if(nsPath.isEmpty) {
        logger.error(s"not found ns path: ${arrName(0)}")
      }
      nsPath.get + arrName(1) + ".xml"
    } else arrName(0) + ".xml"
  }

  def parseControl(xmlNode: XmlNode,parent:Option[Entity]):Either[String,Entity] = {
    val controlPath = getFullPath(xmlNode)
    val (dic,tmpls) = Utils.getXmlNodeParam(xmlNode)
    val newControl = UISystem.create(controlPath,dic,Some(this.control),tmpls)
    var newEntity:Option[Entity] = None
    var mainTemplate:Option[UITemplate] = None
    newControl match {
      case Left(value) => return Left(value)
      case Right(control) =>
        newEntity = control.entity
        mainTemplate = control.template
        control.entity.foreach(_.setParent(parent))
    }
    val childrenSlot = mainTemplate.map(_.slots.get("Children"))
    if(childrenSlot.isEmpty) {
      return Right(newEntity.get)
    }
    for(child <- xmlNode.children.getOrElse(js.Array());if !child.tag.startsWith("Param.")) {
      if(child.tag.startsWith("Slot.")) {
        this.slots.put(child.tag.substring("Slot.".length),newEntity.get)
      } else {
        this.parse(child,newEntity) match {
          case Left(errString) => logger.error(errString)
          case Right(e) => e.setParent(newEntity)
        }
      }
    }
    Right(newEntity.get)
  }
}
