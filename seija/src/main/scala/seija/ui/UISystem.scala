package seija.ui
import scala.scalajs.js
import seija.data.XmlNode
import seija.data.Xml
import seija.data.XmlExt._
import seija.ui.controls.Image
import seija.ui.controls.Panel
import seija.ui.controls.Frame

object UISystem {
  val ENV: js.Dictionary[Any] = js.Dictionary()

  protected var rootPath: String = ""
  def setRootPath(path: String) = rootPath = path

  protected val cacheXml: js.Dictionary[XmlNode] = js.Dictionary()
  protected val creators:js.Dictionary[ControlCreator[Control]] = js.Dictionary()

  
  def init(path:String) {
      setRootPath(path)
      SExprContent.init()
      this.addCreator[Image]()
      this.addCreator[Panel]()
      this.addCreator[Frame]()
  }
  
  def getXml(path: String): Either[String, XmlNode] = {
    val cacheNode = cacheXml.get(path)
    cacheNode match {
      case Some(value) => Right(value)
      case None =>
        val fXml = Xml.fromFile(path)
        fXml.foreach(this.cacheXml.put(path,_))
        fXml.left.map(path + " " + _)
    }
  }

  def addCreator[T <: Control]()(implicit  creator:ControlCreator[T]) {
      creator.init()
      this.creators.put(creator.name,creator)
  }

  def createByFile(path: String,parent: Option[Control],param: ControlParams): Either[String, Control] = {
      this.getXml(rootPath + path).flatMap(this.createByXml(_,parent,param))
  }

  def createByString(str:String,parent: Option[Control],param: ControlParams): Either[String, Control] = {
      Xml.fromString(str).flatMap(createByXml(_,parent,param))
  }

  def createByXml(node: XmlNode,parent: Option[Control],param: ControlParams
                 ,nsPaths:js.Dictionary[String] = js.Dictionary()): Either[String, Control] = {      
      this.scanParams(node,param)
      if(node.tag.indexOf(":") > 0) {
        var arr = node.tag.split(":")
        val prixPath = nsPaths.get(arr(0)).getOrElse("")
        createByFile(prixPath + arr(1) + ".xml",parent,param)
      } else {
        this.create(node.tag,parent,param)
      }
  }

  def create(controlName:String,parent:Option[Control],param:ControlParams):Either[String,Control] = {
      val creator = this.creators.get(controlName)
      if(creator.isEmpty) {
          return Left(s"not found control creator $controlName")
      }
      val newControl = creator.get.create()
      newControl.init(parent,param)
      newControl.OnEnter()
      Right(newControl)
  }


  def scanParams(xmlNode:XmlNode,param:ControlParams) {
      for((attrKey,attrValue) <- xmlNode.attrs;if !param.paramStrings.contains(attrKey)) {
        if(attrKey.startsWith("xmlns:")) {
          val nsName = attrKey.substring("xmlns:".length())
          param.nsPaths.put(nsName,attrValue)
        } else {
          param.paramStrings.put(attrKey,attrValue)
        }
      }
      if(xmlNode.children.isEmpty) return
      for(childNode <- xmlNode.children.get;if childNode.tag.startsWith("Param.")) {
          val paramName = childNode.tag.substring("Param.".length())
          if(childNode.children.isDefined && childNode.children.get.length > 0) {
              if(!param.paramXmls.contains(paramName))
                 param.paramXmls.put(paramName,childNode)
          } else {
              if(!param.paramStrings.contains(paramName))
                 param.paramStrings.put(paramName,childNode.text.getOrElse(""))
          }
      }
  }

  def findEnv(name:String):Any = {
    val nameArrays = name.split('.')
    var curValue:Any = ENV
    for(name <- nameArrays) {
      val curDic = curValue.asInstanceOf[js.Dictionary[Any]]
      curDic.get(name) match {
        case Some(value) =>
          curValue = value
        case None =>
          return null
      }
    }
    curValue
  }
}
