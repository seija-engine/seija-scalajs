package seija.ui
import scala.scalajs.js
import seija.data.XmlNode
import seija.data.Xml
import seija.data.XmlExt._
import seija.ui.controls.Image
import seija.ui.controls.Panel
import seija.ui.controls.Frame
import slogging.LazyLogging

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

  def getCreator(name:String):Option[ControlCreator[Control]] = {
    this.creators.get(name)
  }

  def createByFile(path: String,parent: Option[Control],param: ControlParams): Either[String, Control] = {
      getXml(rootPath + path).flatMap(xmlNode => {
        val fileScope = FileScope(xmlNode)
        fileScope.create(parent)
        fileScope.control.toRight("not found" + rootPath + path)
      })
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

case class FileScope(xmlNode:XmlNode,nsPaths:js.Dictionary[String] = js.Dictionary(),var control:Option[Control] = None) extends LazyLogging {
  def create(parent:Option[Control]) {
    this.scanXmlns()
    val controlParam = ControlParams()
    this.scanParams(xmlNode,controlParam)
    val creator = UISystem.getCreator(xmlNode.tag)
    if(creator.isEmpty) {
      logger.error(s"not find creator ${xmlNode.tag}")
    }
    val newControl = creator.get.create()
    this.control = Some(newControl)
    
    newControl.init(parent,controlParam)
  }


  def scanParams(xmlNode:XmlNode,param:ControlParams) {
      for((attrKey,attrValue) <- xmlNode.attrs;if !param.paramStrings.contains(attrKey)) {
          param.paramStrings.put(attrKey,attrValue)
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

  def scanXmlns() {
    for((k,v) <- this.xmlNode.attrs; if k.startsWith("xmlns:")) {
      this.nsPaths.put(k.substring("xmlns:".length()),v)
    }
  }
}