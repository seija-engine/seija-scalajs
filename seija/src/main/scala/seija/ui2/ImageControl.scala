package seija.ui2

import seija.data.{Color, SExpr, SInt, SKeyword, SUserData, XmlNode}
import seija.math.{Vector2, Vector3}

import scala.scalajs.js

class ImageControl extends Control {
  override def init():Unit = {
    super.init()
    this.property.put("position",Vector3.New(0,0,0))
    this.property.put("scale",Vector3.New(1,1,1))
    this.property.put("rotation",Vector3.New(0,0,0))
    this.property.put("size",Vector2.New(100,100))
    this.property.put("anchor",Vector2.New(0.5f,0.5f))
    this.property.put("color",Color.New(1,1,1,1))
    this.property.put("texture",0)

    this.property.put("Int",0)
  }

  override def handleEvent(evData: js.Array[SExpr]): Unit = {
    super.handleEvent(evData)
    evData.head.castKeyword() match {
      case ":ClickImage" =>
        val oldInt = this.property("Int").asInstanceOf[Int]
        this.setProperty("Int",oldInt + 1)
        this.emit(":UpdateInt",SInt(oldInt + 1)  )
      case _ => ()
    }
  }
}


object ImageControl {
  def create(xmlNode: XmlNode):Either[String,ImageControl] = {
     if(xmlNode.children.isEmpty) {
       return Left("need children")
     }
     val image = new ImageControl
     for(node <- xmlNode.children.get) {
       if(node.tag == "Template") {
         image.template = Some(new UITemplate(node,image))
       }
     }
    image.init()
    Right(image)
  }
}