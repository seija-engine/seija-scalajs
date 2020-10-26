package core

import data.XmlExt._
import data.XmlNode

import scala.collection.immutable.NumericRange
import scala.collection.mutable
import scala.scalajs.js


case class Template(private val xmlNode: XmlNode) {
  def call(dic:js.Dictionary[js.Any]):Entity = {
    this.createEntityByXml(xmlNode,None).get
  }

  private def createEntityByXml(node:XmlNode,parent:Option[Entity]):Option[Entity] = {
    node.tag match {
      case "Entity" =>
        var newEntity = Entity.New();
        newEntity.setParent(parent);
        if(node.children.isDefined) {
          for(n <- node.children.get) {
            n.tag match {
              case "Components" =>
                n.children.map(_.foreach(attachComponent(newEntity,_)))
              case "Entity" => createEntityByXml(n,Some(newEntity))
            };
          }
        }
        Some(newEntity)
      case "Ref" => Some(Entity.New())
      case _ => None
    }
  }

  private def attachComponent(entity:Entity,node:XmlNode):Unit = {
    var opt = Template.components(node.tag);
    opt.attachComponent(entity,node.attrs);

  }
}


object Template {
  var _rootPath:String = "";
  def rootPath:String = _rootPath;
  def setRootPath(path:String):Unit = _rootPath = path

  private var cacheTemplates:mutable.HashMap[String,Template] = mutable.HashMap();
  private var components:mutable.HashMap[String,TemplateComponent] = mutable.HashMap();

  def fromXmlFile(path:String):Either[String,Template] = {
    if(this.cacheTemplates.contains(path)) {
      return Right(this.cacheTemplates(path))
    }

    var xmlNode = data.Xml.fromFile(_rootPath + path);
    xmlNode match {
      case Left(err) => Left(err)
      case Right(xmlNode) =>
        var tmpl = new Template(xmlNode);
        this.cacheTemplates.put(path,tmpl);

        var depFiles = scanDepFiles(tmpl)
        for(file <- depFiles) {
          var ret = Template.fromXmlFile(file)
          if(ret.isLeft) {
            return ret;
          }
        }
        Right(tmpl)
    }
  }

  def scanDepFiles(tmpl:Template): js.Array[String] = {
    var refs:js.Array[XmlNode] = tmpl.xmlNode.searchTagNode("Ref");
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
  def attachComponent(entity:Entity,attrs:js.Dictionary[String])
}

sealed trait TemplateParam
case class TemplateConstParam(value:String) extends TemplateParam
case class TemplateVarParam(varNames:js.Array[String]) extends TemplateParam
case class TemplateSeqParam(array: js.Array[TemplateParam]) extends TemplateParam

class TmplParamParser(var chars:Array[Char]) {
  var curIndex:Int = -1;
 

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
    this.chars = str.toCharArray;
    this.curIndex = -1;
    var mSpace = this.takeSpace();
    if(this.isEnd) {
      return Right(TemplateConstParam(mSpace))
    }
    var firstChar = this.lookNext(1);
    if(firstChar.contains('@')) {
      this.moveNext();
      var constString = this.takeWhile(_ => true);
      return Right(TemplateConstParam(constString))
    }
    if(firstChar.contains('{')) {
      this.moveNext();
      this.takeDotVarList().flatMap((dotArray) => {
        this.takeSpace()
        var lookNext = this.lookNext(1);
        if (lookNext.contains('|')) {
          this.moveNext();
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
    this.takeSpace();
    var arr:js.Array[TemplateParam] = js.Array();
    var isEnd = false;
    var isNeedNextOr = false;
    var errString:String = "";
    while (!isEnd) {
      this.lookNext(1) match {
        case Some(value)  =>
          if(isIdentChar(value)) {
            if(isNeedNextOr) {
              errString = "error format need |"
              isEnd = true;
            } else {
              var dotVar = this.takeDotVar()
              arr.push(dotVar);
              isNeedNextOr = true;
            }
          } else if(value == ' ') {
            this.takeSpace();
          } else if (value == '|') {
            isNeedNextOr = false;
            this.moveNext();
          } else if (value == '}') {
            this.moveNext();
            isEnd = true
          } else {
            errString = "error Char " + value
            isEnd = true
          }
        case None => isEnd = true
      }
    }
    if(errString == "") Right(arr) else Left(errString)
  }

  def takeDotVar(): TemplateParam = {
    var retArray:js.Array[String] = js.Array()
    while(this.lookNext(1).exists(isIdentChar)) {
      var ident = this.takeWhile(isIdentChar)
      retArray.push(ident);
      if(this.lookNext(1).contains('.')) {
        this.moveNext()
      }
    }
    TemplateVarParam(retArray)
  }

  def takeWhile(f:(Char) => Boolean):String = {
    var retString:String = "";
    do {
      this.lookNext(1) match {
        case Some(value) =>
          if(f(value)) {
            retString += value;
            this.moveNext()
          } else {
            return retString
          }
        case None => return retString
      }
    } while(true);
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


}

