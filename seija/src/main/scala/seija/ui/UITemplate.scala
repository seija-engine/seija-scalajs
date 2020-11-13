package seija.ui

import seija.core.Entity
import seija.data.{SExpr, SExprParser, XmlNode}

class UITemplate {
}


object UITemplate {
  def create(xmlNode:XmlNode):UITemplate = {
    if(xmlNode.children.isEmpty) {
      return new UITemplate
    }
    xmlNode.children.get.head match {
      case node if node.tag == "Entity" =>
        this.parseEntity(node)
      case node =>
    }
    new UITemplate
  }

  def parseEntity(xmlNode: XmlNode):Entity = {
    val newEntity = Entity.New()
    if(xmlNode.children.isDefined) {
      for(node <- xmlNode.children.get) {
        node.tag match {
          case "Components" =>
            node.children.foreach(arr => {
              for(compNode <- arr) {
                UIComponent.attach(newEntity,compNode)
              }
            })
          case nodeTagName =>
        }
      }
    }
    newEntity
  }

  def parseParam(string: String):Either[String,SExpr] = {
    if(string.length  == 0) {
      return Left("")
    }
    string.head match {
      case '(' =>
        SExprParser.parse(string) match {
          case Left(value) =>
            println(value)
            Left("error")
          case Right(value) => Right(value)
        }
      case str => Left(string.tail)
    }
  }
}