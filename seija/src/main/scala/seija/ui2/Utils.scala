package seija.ui2

import seija.data.{SExpr, SExprParser, XmlNode}

import scala.scalajs.js

object Utils {
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
      case str => Left(string)
    }
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
}
