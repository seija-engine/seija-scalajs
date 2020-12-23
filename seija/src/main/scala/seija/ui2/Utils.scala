package seija.ui2

import seija.data.{SExpr, SExprParser, XmlNode}

import scala.scalajs.js

object Utils {
  def parseParam(string: String):Either[String,SExpr] = {
    if(string.isEmpty) {
      return Left("")
    }
    if(string.head == '(' || string.startsWith("#(")) {
       SExprParser.parse(string) match {
          case Left(value) =>
            println(value)
            Left("error")
          case Right(value) => Right(value)
        }
    } else {
      Left(string)
    }
  }

  def getXmlNodeParam(xmlNode:XmlNode):(js.Dictionary[String],js.Dictionary[XmlNode]) = {
    val tmpls:js.Dictionary[XmlNode] = js.Dictionary()
    if(xmlNode.children.isDefined) {
      for(item <- xmlNode.children.get) {
        if(item.tag.startsWith("Param.")) {
          if(item.tag.endsWith("Template")) {
            tmpls.put(item.tag.substring("Param.".length),item)
          } else {
            xmlNode.attrs.put(item.tag.drop(6),item.text.getOrElse(""))
          }
        }
      }
    }
    (xmlNode.attrs,tmpls)
  }

  def getXmlStringParam(xmlNode: XmlNode):js.Dictionary[String] = {
    if(xmlNode.children.isDefined) {
      for(item <- xmlNode.children.get) {
        if(item.tag.startsWith("Param.")) {
          xmlNode.attrs.put(item.tag.drop(6),item.text.getOrElse(""))
        }
      }
    }
    xmlNode.attrs
  }
}
