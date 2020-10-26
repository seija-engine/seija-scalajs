package core

import data.XmlExt._
import data.XmlNode

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
case class TemplateVarParam(varName:String) extends TemplateParam
case class TemplateSeqParam(array: Array[TemplateParam]) extends TemplateParam

class TmplParamParser(var chars:Array[Char]) {
  var curIndex:Int = -1;
 
  
 

  def moveNext():Unit = this.curIndex += 1;

  def lookNext(n:Int):Option[Char] = {
    if(this.chars.length > this.curIndex + n) {
      return Some(this.chars(this.curIndex + n))
    }
    None
  }

  def isEnd():Boolean = this.curIndex == (this.chars.length - 1)



  def parse(str:String):TemplateParam = {
    this.chars = str.toCharArray();
    this.curIndex = -1;
    var mSpace = this.skipSpace();
    if(this.isEnd) {
      return TemplateConstParam(mSpace.getOrElse(""))
    }
    this.lookNext(1) match {
      case Some('{') => this.parseVar()
      case _ => ()
    };

    TemplateConstParam("")
  }

  def parseVar():Unit = {

  }

  def skipSpace():Option[String] = {
    var spaceString = "";
    do {
      this.lookNext(1) match {
        case Some(' ') => 
          this.moveNext()
          spaceString += ' ';
        case v => 
          if(spaceString == "") {
            return None
          } else {
            return Some(spaceString)
          }
      }
    } while(true);
    None
  }

}

object TemplateParam {
  private var parser:TmplParamParser = new TmplParamParser(Array())
  def parse(str:String):TemplateParam = {
    if(str == "") {
      return TemplateConstParam("")
    }
    this.parser.parse(str);
    TemplateConstParam("0,0,0")
  }
  /*
  def parse(str:String):TemplateParam = {
    var trimString = str.trim();
    var charIter = trimString.toCharArray().toIterator;
    if(charIter.isEmpty) {
      return TemplateConstParam("")
    }
    var eFirstChar = this.takeFirst(charIter);
    if(eFirstChar.isLeft) {
      return TemplateConstParam(eFirstChar.left.get)
    }
    val firstChar = eFirstChar.getOrElse(' ');
    if(firstChar == '{') {
      this.parseVarParam(charIter)
    }

    TemplateConstParam("0,0,0")
  }

  def takeFirst(chars:Iterator[Char]):Either[String,Char] = {
    var retString:String = "";
    while (chars.hasNext) {
      val chr = chars.next();
      retString = retString + chr;
      if(chr != ' ') {
        return Right(chr);
      }
    }
    Left(retString)
  }

  def parseVarParam(chars:Iterator[Char]):Unit = {
    this.skipSpace(chars)
  }

  def skipSpace(chars:Iterator[Char]):Unit = {
    
  }
  */
}

