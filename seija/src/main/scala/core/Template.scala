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

  def parseParam(str:String):TemplateParam = {
    TemplateConstParam("0,0,0")
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