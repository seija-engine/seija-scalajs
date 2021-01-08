package seija.ui
import scala.scalajs.js
import seija.data.XmlNode
import seija.data.Xml
import seija.data.XmlExt._
import seija.ui.controls.Image
import seija.ui.controls.Panel
import seija.ui.controls.Frame
import slogging.LazyLogging
import seija.ui.controls.{Grid,GridCell}
import seija.ui.controls.Menu



object UISystem extends LazyLogging {
  val ENV: js.Dictionary[Any] = js.Dictionary()

  protected var rootPath: String = ""
  def setRootPath(path: String): Unit = rootPath = path

  protected val cacheXml: js.Dictionary[XmlNode] = js.Dictionary()
  protected val creators:js.Dictionary[ControlCreator[Control]] = js.Dictionary()

  
  def init(path:String) {
      setRootPath(path)
      SExprContent.init()
      this.addCreator[Image]()
      this.addCreator[Panel]()
      this.addCreator[Frame]()
      this.addCreator[Grid]()
      this.addCreator[GridCell]()
      this.addCreator[Menu]()
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

  def createByFile(path: String,parent: Option[Control],param: ControlParams,ownerControl:Option[Control]): Either[String, Control] = {
   
    getXml(rootPath + path).map(applyXmlNs).flatMap(createByXml(_,parent,param,None))
  }

  def createByXml(xmlNode:XmlNode,parent:Option[Control],param:ControlParams,ownerControl:Option[Control]):Either[String,Control] = {
    this.scanParams(xmlNode,param)
    if(xmlNode.tag.indexOf(":") > 0) {
       createByFile(xmlNode.attrs("nsFilePath"),parent,param,ownerControl)
    } else {
      val creator = this.getCreator(xmlNode.tag)
      if(creator.isEmpty) {
        return Left(s"not found control ${xmlNode.tag}")
      }
      val newControl = creator.get.create()
      newControl.init(parent,param,ownerControl)
      Right(newControl)
    }
  }

  def scanParams(xmlNode:XmlNode,param:ControlParams,autoChild:Boolean = true) {
      logger.trace(s"${xmlNode.tag}")
      for((attrKey,attrValue) <- xmlNode.attrs;if !param.paramStrings.contains(attrKey)) {
          param.paramStrings.put(attrKey,attrValue)
      }
      if(xmlNode.children.isEmpty) return
      
      for(childNode <- xmlNode.children.get) {
          if(childNode.tag.startsWith("Param.")) {
             val paramName = childNode.tag.substring("Param.".length())
             if(childNode.children.isDefined && childNode.children.get.length > 0) {
                 if(!param.paramXmls.contains(paramName))
                  param.paramXmls.put(paramName,childNode)
               } else {
                   if(!param.paramStrings.contains(paramName))
                    param.paramStrings.put(paramName,childNode.text.getOrElse(""))
              }
          } else if(childNode.tag.indexOf(".") <= 0) {
            param.children.push(childNode)
          }
      }
  }

  def applyXmlNs(xmlNode: XmlNode): XmlNode = {
    val nsPaths = xmlNode.attrs.filter(_._1.startsWith("xmlns:"))
                               .foldLeft[js.Dictionary[String]](js.Dictionary())((d,v) => {d.put(v._1.substring("xmlns:".length()),v._2);d})
    def depSearchApply(nsPaths:js.Dictionary[String],xmlNode:XmlNode) {
      if(xmlNode.tag.indexOf(":") > 0) {
        val karr = xmlNode.tag.split(":")
        val v = nsPaths(karr(0)) + karr(1) + ".xml"
        xmlNode.attrs.put("nsFilePath",v)
      }
      if(xmlNode.children.isDefined) {
        for(child <- xmlNode.children.get) {
          depSearchApply(nsPaths,child)
        }
      }
    }
    depSearchApply(nsPaths,xmlNode)
    xmlNode
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