package seija.ui2

import seija.data.{Color, XmlNode}
import seija.math.{Vector2, Vector3}

class ImageControl extends Control {
  override def init():Unit = {
    super.init()
    this.Property.put("position",Vector3.New(0,0,0))
    this.Property.put("scale",Vector3.New(1,1,1))
    this.Property.put("rotation",Vector3.New(0,0,0))
    this.Property.put("size",Vector2.New(100,100))
    this.Property.put("anchor",Vector2.New(0.5f,0.5f))
    this.Property.put("color",Color.New(1,1,1,1))
    this.Property.put("texture",0)
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