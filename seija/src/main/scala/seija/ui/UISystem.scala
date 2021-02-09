package seija.ui
import scala.scalajs.js
import seija.data.XmlNode
import seija.data.Xml
import seija.data.XmlExt._
import seija.ui.controls._
import slogging.LazyLogging
import seija.core.Entity
import seija.core.Transform
import seija.s2d.Rect2D
import seija.core.event.EventNode
import seija.s2d.layout.ContentView
import seija.core.event.CABEventRoot
import seija.math.Vector3


object UISystem extends LazyLogging {
  val ENV: js.Dictionary[Any] = js.Dictionary()

  protected var rootPath: String = ""
  def setRootPath(path: String): Unit = rootPath = path

  protected val cacheXml: js.Dictionary[XmlNode] = js.Dictionary()
  protected val creators:js.Dictionary[ControlCreator[Control]] = js.Dictionary()
  
  protected var layers:js.Array[UILayer] = js.Array()
  val layerNameDic:js.Dictionary[UILayer] = js.Dictionary()
  protected var uiRoot:Option[Entity] = None

  
  def init(path:String,layerNames:js.Array[String] = js.Array()) {
      setRootPath(path)
      SExprContent.init()
      this.addCreator[Image]()
      this.addCreator[Panel]()
      this.addCreator[Frame]()
      this.addCreator[Stack]()
      this.addCreator[Grid]()
      this.addCreator[GridCell]()
      this.addCreator[Menu]()
      this.addCreator[Label]()
      this.addCreator[ContextMenu]()
      this.addCreator[Sprite]()
      this.addCreator[SelectBox]()
      this.addCreator[Dialog]()
      this.addCreator[Input]()
      this.addCreator[RawInput]()
      this.addCreator[SelectFile]()
      this.addCreator[Button]()
      this.addCreator[ListBox]()

      val root = Entity.New(None)
      root.addComponent[Transform]()
      root.addComponent[Rect2D]()
      root.addComponent[ContentView]()
      root.addComponent[CABEventRoot]()
      this.uiRoot = Some(root)
      this.createLayers(root,layerNames)
  }

  def createLayers(parent:Entity,layerNames:js.Array[String]) {
    var idx = layerNames.length;
    for(name <- layerNames) {
        val layer = UILayer.create(parent,name,idx);
        layers.push(layer)
        this.layerNameDic.put(name,layer)
        idx -=1
    }
  }

  def getLayer(layerName:String):Option[UILayer] = {
    this.layerNameDic.get(layerName)
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

  def createByFile(path:String,
                   parent: Option[Control],
                   param: ControlParams,
                   ownerControl:Option[Control]): Either[String, Control] = {
    
    getXml(rootPath + path).map(applyXmlNs).flatMap(createByXml(_,parent,param,ownerControl))
  }

  def createByXml(xmlNode:XmlNode,
                  parent:Option[Control],
                  param:ControlParams,
                  ownerControl:Option[Control]):Either[String,Control] = {
    this.scanParams(xmlNode,param)
    if(xmlNode.tag.indexOf(":") > 0) {
       if(xmlNode.attrs.contains("nsFilePath")) {
          createByFile(xmlNode.attrs("nsFilePath"),parent,param,ownerControl)
       } else {
         val errString = s"not found nsFilePath ${xmlNode.tag}";
         Left(errString)
       }
      
    } else {
      val creator = this.getCreator(xmlNode.tag)
      if(creator.isEmpty) {
        return Left(s"not found control ${xmlNode.tag}")
      }
      val newControl = creator.get.create()
      //logger.error(s"${xmlNode.tag} = ${ownerControl.toString()}")
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
          } else if(childNode.tag.startsWith("Slot.")) {
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
        if(nsPaths.contains(karr(0))) {
          val v = nsPaths(karr(0)) + karr(1) + ".xml"
          xmlNode.attrs.put("nsFilePath",v)
        }
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

  def Update() = this.layers.foreach(_.UpdateDirtyZOrder())

}