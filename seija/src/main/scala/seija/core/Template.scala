package seija.core

import Template.get
import seija.data.XmlExt.RichXmlNode
import seija.data.{Read, Xml, XmlNode}

import scala.collection.mutable
import scala.scalajs.js

class TemplateIDRef(private var dic:js.Dictionary[Entity],private var children:js.Dictionary[TemplateIDRef]) {
  def set(id:String,e:Entity):Unit = {
    this.dic.put(id,e)
  }
  def setChild(id:String,ref:TemplateIDRef):Unit = children.put(id,ref)

  def find(string: String):Option[Entity] = {
    val arr = string.split('.')
    println("len:" + arr.length+" s:"+string)
    if(arr.length == 0) return dic.get(string)
    var curRef = this;
    for(idx <- 0 to arr.length - 2) {
      val s = arr(idx)
      curRef.children.get(s) match {
        case Some(r) =>
          curRef = r
        case None =>
          return None
      }
    }
    curRef.dic.get(arr.last)
  }
}

case class Template(private val xmlNode: XmlNode) {
  def call(data:js.Dictionary[Any]):(Entity,TemplateIDRef) = {
    val templateIDRef = new TemplateIDRef(js.Dictionary(),js.Dictionary())
    val entity = this.createEntityByXml(xmlNode,None,data,None,js.Dictionary(),Some(templateIDRef)).get
    (entity,templateIDRef)
  }

  private def createEntityByXml(node:XmlNode, parent:Option[Entity], data:js.Dictionary[Any],
                                parentConst:Option[js.Dictionary[String]],
                                entityParams:js.Dictionary[js.Array[XmlNode]],
                                idRef:Option[TemplateIDRef]):Option[Entity] = {

    def handleRef(refNode:XmlNode,refParent:Option[Entity],parentRef:Option[TemplateIDRef]):Option[Entity] = {
      val (getData,constData) = this.attrsToData(refNode.attrs,data)
      val entityParams:js.Dictionary[js.Array[XmlNode]] = js.Dictionary();
      refNode.children.foreach(childes => {
        for(refChildNode <- childes; if refChildNode.tag.startsWith("Param.")) {
          val nodeParamName = refChildNode.tag.slice(6,refChildNode.tag.length)
          if(refChildNode.children.isDefined) {
            entityParams.put(nodeParamName,refChildNode.children.get)
          }
        }
      })
      Template.fromXmlFile(refNode.attrs("src")) match {
        case Left(err) =>
          println(s"load Ref error:$err")
          None
        case Right(tmpl) =>
          val needSet = refNode.attrs.contains("id") && parentRef.isDefined
          val newIdRef = if(needSet) {
            val newValue = new TemplateIDRef(js.Dictionary(),js.Dictionary())
            parentRef.get.setChild(refNode.attrs("id"),newValue)
            Some(newValue)
          } else {
            None
          }
          val e = createEntityByXml(tmpl.xmlNode,refParent,getData,Some(constData),entityParams,newIdRef)
          if(needSet) {
            parentRef.get.set(refNode.attrs("id"),e.get)
          }
          e
      }
    }

    node.tag match {
      case "Entity" =>
        var newEntity = Entity.New()
        newEntity.setParent(parent)
        if(node.attrs.contains("id")) {
          idRef.foreach(_.set(node.attrs("id"),newEntity))
        }
        if(node.children.isDefined) {
          for(n <- node.children.get) {
            n.tag match {
              case "Components" =>
                n.children.map(_.foreach(attachComponent(newEntity,_,data,parentConst)))
              case "Entity" =>
                var e = createEntityByXml(n,Some(newEntity),data,None,js.Dictionary(),idRef)
                if(node.attrs.contains("id")) {
                  idRef.foreach(_.set(node.attrs("id"),e.get))
                }
              case "Ref" =>
                handleRef(n,Some(newEntity),idRef)
              case s if s.startsWith("UseParam.") =>
                val nodeParamName = s.slice(9,s.length)
                val paramXmlNodes = entityParams.get(nodeParamName)
                if(paramXmlNodes.isDefined) {
                  for(node <- paramXmlNodes.get) {
                    createEntityByXml(node,Some(newEntity),data,parentConst,entityParams,idRef);
                  }
                }
                None
              case _ => None
            }
          }
        }
        Some(newEntity)
      case "Ref" => handleRef(node,parent,idRef)
      case _ => None
    }
  }

  private def attachComponent(entity:Entity,node:XmlNode,data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]]):Unit = {
    Template.components(node.tag).attachComponent(entity,node.attrs,data,parentConst)
  }

  private def attrsToData(attrs:js.Dictionary[String],data:js.Dictionary[Any]):(js.Dictionary[Any],js.Dictionary[String]) = {
    var retData:js.Dictionary[Any] = js.Dictionary();
    var retConst:js.Dictionary[String] = js.Dictionary();
    for((attrKey,attrValue) <- attrs) {
      if(attrKey != "src") {
        TemplateParam.parse(attrValue) match {
          case Left(value) =>
            println(s"Ref attr $attrKey error $value")
          case Right(value) =>
            value.readVarOrConst(data) match {
              case Left(value) =>
                println(s"Ref attr $attrKey error $value")
              case Right(Left(value)) =>
                retConst.put(attrKey,value)
              case Right(Right(value)) =>
                retData.put(attrKey,value)
            }
        }
      }
    }
    (retData,retConst)
  }
}


object Template {
  var _rootPath:String = ""
  def rootPath:String = _rootPath
  private var _env:js.Dictionary[Any] = js.Dictionary()
  def setRootPath(path:String):Unit = _rootPath = path
  def env:js.Dictionary[Any] = _env

  private var cacheTemplates:mutable.HashMap[String,Template] = mutable.HashMap()
  private var components:mutable.HashMap[String,TemplateComponent] = mutable.HashMap()

  def fromXmlFile(path:String):Either[String,Template] = {
    if(this.cacheTemplates.contains(path)) {
      return Right(this.cacheTemplates(path))
    }

    var xmlNode = Xml.fromFile(_rootPath + path)
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
    val refs:js.Array[XmlNode] = tmpl.xmlNode.searchTagNode("Ref")
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
  def attachComponent(entity:Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]])
}

sealed trait TemplateParam {
  def readVarOrConst(data:js.Dictionary[Any]):Either[String,Either[String,Any]] = {
    this match {
      case TemplateConstParam(constString) => Right(Left(constString))
      case TemplateVarParam(seqNames) =>
        if(seqNames.length > 0) {
          val useData:js.Dictionary[Any] = if(seqNames(0) == "params") data
                                           else if(seqNames(0) == "env") Template.env
                                           else js.Dictionary();
          seqNames.remove(0)
          TemplateParam.findObjectValue(seqNames,useData) match {
            case Some(d) => Right(Right(d))
            case None => Left(s"not find data ${seqNames.toString}")
          }
        } else Left("zero params")
      case TemplateSeqParam(seq) =>
        for(param <- seq) {
          val value = param.readVarOrConst(data)
          if(value.isRight) {
            return value
          }
        }
        Left(s"not find data ${seq.toString}")
    }
  }
}
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

  def setValueByAttrDic[T](attrs:js.Dictionary[String], name:String, f:T=>Unit, data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]])(implicit readT:Read[T]):Either[String,Unit] = {
    val attrValue = attrs.get(name)
    if(attrValue.isEmpty) {
      return Right()
    }
    for {
      attrString <- attrValue.toRight("")
      param <- TemplateParam.parse(attrString)
      setRet <- this.setTo(param,f,data,parentConst)(readT)
    } yield setRet
  }

  def setTo[T](param:TemplateParam,f: T =>Unit,data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]])(implicit readT:Read[T]):Either[String,Unit] = {
    this.readParamValue(param,data,parentConst)(readT) match {
      case Some(value) =>
        //println("set "+value.toString)
        f(value)
        Right()
      case None =>
        Left(s"read value ${param.toString}")
    }
  }

  def readParamValue[T](param:TemplateParam,data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]])(implicit readT:Read[T]):Option[T] = {
    param match {
      case TemplateConstParam(value) => readT.read(value)
      case TemplateVarParam(varNames) =>
        if(varNames.length > 0) {
          val useData:js.Dictionary[Any] = if(varNames(0) == "params") data
                                           else if(varNames(0) == "env") Template.env
                                           else js.Dictionary();
          varNames.remove(0)
          this.findObjectValue(varNames, useData).map(_.asInstanceOf[T]) match {
            case Some(value) => Some(value)
            case None => parentConst.flatMap(d => d.get(varNames(0))).flatMap(readT.read)
          }
        } else None
      case TemplateSeqParam(array) =>
        for(item <- array) {
          this.readParamValue(item,data,parentConst) match {
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

