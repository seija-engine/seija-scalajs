package seija.ui2

import seija.core.event.{EventNode, GameEventType}
import seija.core.{Entity, Transform}
import seija.data.{Color, SBool, SExprInterp, SFunc, SList, SNil, SUserData, SVector, XmlNode}
import seija.math.{Vector2, Vector3}
import seija.s2d.{ImageRender, Rect2D, SpriteRender, Transparent}
import seija.s2d.assets.{Image, SpriteSheet}
import seija.data.CoreRead._

class TransformUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val trans = entity.addComponent[Transform]()
    val dic = Utils.getXmlNodeParam(xmlNode)
    UIComponent.initParam[Vector3]("position",dic,trans.localPosition = _,tmpl.control.sContent);
    UIComponent.initParam[Vector3]("scale",dic,trans.scale = _,tmpl.control.sContent);
    UIComponent.initParam[Vector3]("rotation",dic,trans.rotation = _,tmpl.control.sContent);
  }
}

class Rect2DUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val rect2d = entity.addComponent[Rect2D]()
    val dic = Utils.getXmlNodeParam(xmlNode)
    UIComponent.initParam[Vector2]("size",dic,rect2d.size = _,tmpl.control.sContent)
    UIComponent.initParam[Vector2]("anchor",dic,rect2d.anchor = _,tmpl.control.sContent)
  }
}

class ImageRenderUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val image = entity.addComponent[ImageRender]()
    val dic = Utils.getXmlNodeParam(xmlNode)
    UIComponent.initParam[Int]("texture",dic,tex => image.setTexture(new Image(tex)),tmpl.control.sContent)
    UIComponent.initParam[Color]("color",dic,image.color = _,tmpl.control.sContent)
  }
}

class SpriteRenderUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    val sprite = entity.addComponent[SpriteRender]()
    val dic = Utils.getXmlNodeParam(xmlNode)
    UIComponent.initParam[String]("spriteName",dic,sName => sprite.setSpriteName(sName),tmpl.control.sContent)
    UIComponent.initParam[Color]("color",dic,sprite.color = _,tmpl.control.sContent)
    UIComponent.initParam[Int]("sheet",dic,sheet => sprite.setSpriteSheet(new SpriteSheet(sheet)),tmpl.control.sContent)
  }
}

class TransparentUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    entity.addComponent[Transparent]()
  }
}

class EventNodeUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    val eventNode = entity.addComponent[EventNode]()
    val dic = Utils.getXmlNodeParam(xmlNode)
    for((k,v) <- dic) {
      val (head,tail) = k.splitAt(2)
      if(head == "On") {
        val evType = GameEventType.gameEventTypeRead.read(tail)
        if(evType.isDefined) {
          UIComponent.cacheContent.clear()
          UIComponent.cacheContent.parent = Some(tmpl.control.sContent)
          UIComponent.cacheContent.set("event-node",SUserData(eventNode))
          SExprInterp.evalString(v, Some(UIComponent.cacheContent)) match {
            case Right(SVector(list)) =>
              val isCapture = list(0).asInstanceOf[SBool].value
              val f = list(1).asInstanceOf[SFunc]
              eventNode.register(evType.get,isCapture,() => {
                f.call(Some(tmpl.control.sContent))
              })
            case Right(f@SFunc(args, list)) =>
              eventNode.register(evType.get,isCapture = false, () => {
                f.call(Some(tmpl.control.sContent))
              })
            case Right(SNil) => ()
            case Left(value) => println(value)
            case Right(_) => println(s"error event param: $k = $v")
          }
        }
      }
    }
  }
}