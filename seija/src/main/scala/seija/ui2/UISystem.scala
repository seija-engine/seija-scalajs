package seija.ui2
import seija.core.Entity
import seija.data.{Read, SContent, SExpr, SExprInterp, SNFunc, SNil, SString, SUserData, Xml, XmlNode}

import scalajs.js

trait UIComponent {
  def attach(entity: Entity,xmlNode:XmlNode,tmpl:UITemplate):Unit
}

object UISystem {
  var rootPath:String = ""
  private val controlCreators:js.Dictionary[XmlNode => Either[String,Control]] = js.Dictionary()
  private val comps:js.Dictionary[UIComponent] = js.Dictionary()
  private var controlContent:Option[SContent] = None
  def cContent:Option[SContent] = controlContent

  def initCore():Unit = {
    this.registerControl("ImageControl",ImageControl.create)

    this.registerComp("Transform",new TransformUIComp)
    this.registerComp("Rect2D",new Rect2DUIComp)
    this.registerComp("ImageRender",new ImageRenderUIComp)

    val content = new SContent(Some(SExprInterp.rootContent))
    this.controlContent = Some(content)
    content.set("attr", SNFunc(UISystemSFunc.attr))
  }

  def create(path:String):Either[String, Control] = {
    val filePath = rootPath + path
    for {
      xmlNode <- Xml.fromFile(filePath)
      createFn <- this.controlCreators.get(xmlNode.tag).toRight(s"not found control creator $xmlNode.tag")
      control <- createFn(xmlNode)
    } yield control
  }

  def registerControl(name:String,createFn:XmlNode => Either[String,Control]):Unit = {
    this.controlCreators.put(name,createFn)
  }

  def registerComp(compName: String,comp:UIComponent):Unit = {
    this.comps.put(compName,comp)
  }

  def getUIComp(name:String):Option[UIComponent] = {
    this.comps.get(name)
  }
}


object UISystemSFunc {
  def attr(args:js.Array[SExpr],context:SContent):SExpr = {
    val attrName = args(0).asInstanceOf[SString].value
    val control = context.find("control").get.asInstanceOf[SUserData].value.asInstanceOf[Control]
    val ret = control.Property.get(attrName).map(SUserData).getOrElse(SNil)
    println(ret)
    ret
  }
}


object UIComponent {
  def initParam[T](name:String,dic:js.Dictionary[String],setFunc:(T) => Unit,content: SContent)(implicit readT:Read[T]):Unit = {
    dic.get(name).map(Utils.parseParam).foreach {
      case Left(value) =>
        readT.read(value).foreach(setFunc)
        println("set l "+value.toString)
      case Right(value) =>
        SExprInterp.eval(value, Some(content)) match {
          case SUserData(value) =>
            setFunc(value.asInstanceOf[T])
            println("set "+ value.toString)
          case _ => ()
        }

    }
  }
}