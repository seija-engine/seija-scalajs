package core

import data.XmlExt._
import data.XmlNode
import data.Read

import scala.collection.mutable
import scala.scalajs.js


case class Template(private val xmlNode: XmlNode) {
  def call(data:js.Dictionary[Any]):Entity = {
    this.createEntityByXml(xmlNode,None,data).get
  }

  private def createEntityByXml(node:XmlNode,parent:Option[Entity],data:js.Dictionary[Any]):Option[Entity] = {
    node.tag match {
      case "Entity" =>
        var newEntity = Entity.New()
        newEntity.setParent(parent)
        if(node.children.isDefined) {
          for(n <- node.children.get) {
            n.tag match {
              case "Components" =>
                n.children.map(_.foreach(attachComponent(newEntity,_,data)))
              case "Entity" => createEntityByXml(n,Some(newEntity),data)
            }
          }
        }
        Some(newEntity)
      case "Ref" => Some(Entity.New())
      case _ => None
    }
  }

  private def attachComponent(entity:Entity,node:XmlNode,data:js.Dictionary[Any]):Unit = {

    Template.components(node.tag).attachComponent(entity,node.attrs,data)

  }
}


object Template {
  var _rootPath:String = ""
  def rootPath:String = _rootPath
  def setRootPath(path:String):Unit = _rootPath = path

  private var cacheTemplates:mutable.HashMap[String,Template] = mutable.HashMap()
  private var components:mutable.HashMap[String,TemplateComponent] = mutable.HashMap()

  def fromXmlFile(path:String):Either[String,Template] = {
    if(this.cacheTemplates.contains(path)) {
      return Right(this.cacheTemplates(path))
    }

    var xmlNode = data.Xml.fromFile(_rootPath + path)
    xmlNode match {
      case Left(err) => Left(err)
      case Right(xmlNode) =>
        var tmpl = new Template(xmlNode)
        this.cacheTemplates.put(path,tmpl)

        var depFiles = scanDepFiles(tmpl)
        for(file <- depFiles) {
          var ret = Template.fromXmlFile(file)
          if(ret.isLeft) {
            return ret
          }
        }
        Right(tmpl)
    }
  }

  def scanDepFiles(tmpl:Template): js.Array[String] = {
    var refs:js.Array[XmlNode] = tmpl.xmlNode.searchTagNode("Ref")
    refs.map(_.attrs.get("src").getOrElse("")).filter( _ != "")
  }

  def get(path:String):Option[Template] = cacheTemplates.get(path)
  def cacheNames: collection.Set[String] = cacheTemplates.keySet

  def registerComponent(tc: TemplateComponent):Unit = {
    components.put(tc.name,tc)
  }

  
}

trait TemplateComponent {
  val name:String
  def attachComponent(entity:Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any])
}

sealed trait TemplateParam
case class TemplateConstParam(value:String) extends TemplateParam {
  override def toString: String = value
}
case class TemplateVarParam(varNames:js.Array[String]) extends TemplateParam {
  override def toString: String = varNames.fold("")((a,b)=> a + "." + b)
}
case class TemplateSeqParam(array: js.Array[TemplateParam]) extends TemplateParam {
  override def toString: String = s"Seq(${array.fold("")((a,b) => a + "|" + b)})"
}

class TmplParamParser(var chars:Array[Char]) {
  var curIndex:Int = -1
 

  def moveNext():Unit = {
    if(this.curIndex + 1 < this.chars.length) {
      this.curIndex += 1
    }
  }

  def lookNext(n:Int):Option[Char] = {
    if(this.chars.length > this.curIndex + n) {
      return Some(this.chars(this.curIndex + n))
    }
    None
  }

  def isEnd:Boolean = this.curIndex == (this.chars.length - 1)

  def parse(str:String):Either[String,TemplateParam] = {
    this.chars = str.toCharArray
    this.curIndex = -1
    var mSpace = this.takeSpace()
    if(this.isEnd) {
      return Right(TemplateConstParam(mSpace))
    }
    var firstChar = this.lookNext(1)
    if(firstChar.contains('@')) {
      this.moveNext()
      var constString = this.takeWhile(_ => true)
      return Right(TemplateConstParam(constString))
    }
    if(firstChar.contains('{')) {
      this.moveNext()
      this.takeDotVarList().flatMap(dotArray => {
        this.takeSpace()
        var lookNext = this.lookNext(1)
        if (lookNext.contains('|')) {
          this.moveNext()
          var constString = this.takeWhile(_ => true)
          dotArray.push(TemplateConstParam(constString))
        } else if(lookNext.isDefined) {
          return Left("error format need |")
        }
        if (dotArray.length > 1) {
          Right(TemplateSeqParam(dotArray))
        } else if(dotArray.length == 1) {
          Right(dotArray(0))
        } else {
          Left("zero params")
        }
      })
    } else {
      Right(TemplateConstParam(str))
    }

  }

  def takeDotVarList():Either[String,js.Array[TemplateParam]]  = {
    this.takeSpace()
    var arr:js.Array[TemplateParam] = js.Array()
    while (true) {
        this.lookNext(1) match {
          case Some(value) if isIdentChar(value) =>
            var dotVar = this.takeDotVar()
            arr.push(dotVar)
            this.takeSpace()
            this.lookNext(1) match {
              case Some('|') =>
              this.moveNext()
              this.takeSpace()
              case Some('}') =>
                this.moveNext()
                return Right(arr)
              case _ => return Left("error params need | or }")
            }
          case _ => return Left("error format need ident")
        }
    }
    Left("error")
  }

  def takeDotVar(): TemplateParam = {
    val retArray: js.Array[String] = js.Array()
    while(this.lookNext(1).exists(isIdentChar)) {
      val ident = this.takeWhile(isIdentChar)
      retArray.push(ident)
      if(this.lookNext(1).contains('.')) {
        this.moveNext()
      }
    }
    TemplateVarParam(retArray)
  }

  def takeWhile(f: Char => Boolean):String = {
    var retString:String = ""
    do {
      this.lookNext(1) match {
        case Some(value) =>
          if(f(value)) {
            retString += value
            this.moveNext()
          } else {
            return retString
          }
        case None => return retString
      }
    } while(true)
    ""
  }

  def takeSpace():String = this.takeWhile(_ == ' ')

  def takeIdent():String = this.takeWhile(isIdentChar)

  def isIdentChar(chr:Char): Boolean = TemplateParam.numberSet(chr) || TemplateParam.identSet(chr)
}

object TemplateParam {
  private var parser:TmplParamParser = new TmplParamParser(Array())
  val identSet: Set[Char] = ('a' to 'z').toSet ++ ('A' to 'Z').toSet + '_'
  val numberSet:Set[Char] = ('0' to '9').toSet
  def parse(str:String): Either[String, TemplateParam] = this.parser.parse(str)

  def setToByAttrDic[T](attrs:js.Dictionary[String],name:String,f:T=>Unit,data:js.Dictionary[Any])(implicit readT:Read[T]):Either[String,Unit] = {
    val attrValue = attrs.get(name)
    if(attrValue.isEmpty) {
      return Right()
    }
    for {
      attrString <- attrValue.toRight("")
      param <- TemplateParam.parse(attrString)
      setRet <- this.setTo(param,f,data)(readT)
    } yield setRet
  }

  def setTo[T](param:TemplateParam,f: T =>Unit,data:js.Dictionary[Any])(implicit readT:Read[T]):Either[String,Unit] = {
    this.readParamValue(param,data)(readT) match {
      case Some(value) =>
        println("set "+value.toString)
        f(value)
        Right()
      case None => Left(s"not found value ${param.toString}")
    }
  }

  def readParamValue[T](param:TemplateParam,data:js.Dictionary[Any])(implicit readT:Read[T]):Option[T] = {
    param match {
      case TemplateConstParam(value) => readT.read(value)
      case TemplateVarParam(varNames) =>
        if(varNames.length > 0 && varNames(0) == "params") {
          varNames.remove(0)
        }
        this.findObjectValue(varNames,data).map(_.asInstanceOf[T])
      case TemplateSeqParam(array) =>
        for(item <- array) {
          this.readParamValue(item,data) match {
            case Some(value) =>
              return Some(value)
            case None => ()
          }
        }
        None
    }
  }

  def findObjectValue(seqNames:js.Array[String],data:js.Dictionary[Any]):Option[js.Any] = {
    var curValue:js.Any = data;
    for(name <- seqNames) {
      curValue.asInstanceOf[js.Dictionary[js.Any]].get(name) match {
        case Some(value) =>
          curValue = value
        case None => return None
      }
    }
    Some(curValue)
  }

}

