package seija.ui2
import scala.scalajs.js
import seija.core.event.{CABEventRoot, EventNode, GameEventType}
import seija.core.{Entity, Transform}
import seija.data.{Color, SBool, SExprInterp, SFunc, SList, SNil, SUserData, SVector, XmlNode}
import seija.math.{Vector2, Vector3}
import seija.s2d.{ImageRender, ImageType, Rect2D, SpriteRender, TextRender, Transparent}
import seija.s2d.assets.{Image, SpriteSheet}
import seija.data.CoreRead._
import seija.s2d.assets.Font
import seija.s2d.layout.LayoutAlignment.LayoutAlignment
import seija.s2d.layout.Orientation.Orientation
import seija.s2d.layout.{ContentView, GridCell, GridLayout, LNumber, LayoutView, StackLayout, Thickness}

class TransformUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val trans = entity.addComponent[Transform]()
    val dic = Utils.getXmlStringParam(xmlNode)
    UIComponent.initParam[Vector3]("position",dic,trans.localPosition = _,tmpl.control.sContent);
    UIComponent.initParam[Vector3]("scale",dic,trans.scale = _,tmpl.control.sContent);
    UIComponent.initParam[Vector3]("rotation",dic,trans.rotation = _,tmpl.control.sContent);
  }
}

class Rect2DUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val rect2d = entity.addComponent[Rect2D]()
    val dic = Utils.getXmlStringParam(xmlNode)
    UIComponent.initParam[Vector2]("size",dic,rect2d.size = _,tmpl.control.sContent)
    UIComponent.initParam[Vector2]("anchor",dic,rect2d.anchor = _,tmpl.control.sContent)
  }
}

class ImageRenderUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,tmpl:UITemplate): Unit = {
    val image = entity.addComponent[ImageRender]()
    val dic = Utils.getXmlStringParam(xmlNode)
    UIComponent.initParam[Int]("texture",dic,tex => image.setTexture(new Image(tex)),tmpl.control.sContent)
    UIComponent.initParam[Color]("color",dic,color => {
      image.color = color
    },tmpl.control.sContent)
  }
}

class SpriteRenderUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    val sprite = entity.addComponent[SpriteRender]()
    val dic = Utils.getXmlStringParam(xmlNode)
    UIComponent.initParam[String]("spriteName",dic,sName => sprite.setSpriteName(sName),tmpl.control.sContent)
    UIComponent.initParam[Color]("color",dic,sprite.color = _,tmpl.control.sContent)
    UIComponent.initParam[Int]("sheet",dic,sheet => sprite.setSpriteSheet(new SpriteSheet(sheet)),tmpl.control.sContent)
    UIComponent.initParam[ImageType]("type",dic,t => sprite.setImageType(t),tmpl.control.sContent)
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
    val dic = Utils.getXmlStringParam(xmlNode)

    for((k,v) <- dic) {
      val (head,tail) = k.splitAt(2)
      if(head == "On") {

        val evType = GameEventType.gameEventTypeRead.read(tail)
        if(evType.isDefined) {
          UIComponent.cacheContent.clear()
          UIComponent.cacheContent.parent = Some(tmpl.control.sContent)
          UIComponent.cacheContent.set("event-node",SUserData(eventNode))
          val evalValue = SExprInterp.evalString(v, Some(UIComponent.cacheContent))
          evalValue match {
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

class EventBoardUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val eventBoard = entity.addComponent[EventBoardComponent]()
    eventBoard.initBoard(dic.get("name").getOrElse(""))
    tmpl.control.eventBoard = eventBoard.eventBoard
  }
}

class TextRenderUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val textRender = entity.addComponent[TextRender]()
    UIComponent.initParam[String]("text",dic,sText =>textRender.setText(sText),tmpl.control.sContent)
    UIComponent.initParam[Color]("color",dic,textRender.color = _,tmpl.control.sContent)
    UIComponent.initParam[Int]("font",dic,fontID => textRender.setFont(new Font(fontID)) ,tmpl.control.sContent)
    UIComponent.initParam[Int]("fontSize",dic,fontSize => textRender.setFontSize(fontSize),tmpl.control.sContent)
  }
}

class LayoutViewUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val layoutView = entity.addComponent[LayoutView]()
    setViewParam(layoutView,dic,tmpl)
  }

  def setViewParam(layoutView:LayoutView,dic:js.Dictionary[String], tmpl: UITemplate): Unit = {
    UIComponent.initParam[LayoutAlignment]("hor",dic, hor => layoutView.setHor(hor),tmpl.control.sContent)
    UIComponent.initParam[LayoutAlignment]("ver",dic,ver => layoutView.setVer(ver),tmpl.control.sContent)
    UIComponent.initParam[Vector2]("size",dic,s => {
      layoutView.setSize(s)
    },tmpl.control.sContent)
    UIComponent.initParam[Vector3]("position",dic,s => layoutView.setPosition(s),tmpl.control.sContent)
    UIComponent.initParam[Thickness]("margin",dic,t => layoutView.setMargin(t),tmpl.control.sContent)
    UIComponent.initParam[Thickness]("padding",dic,t => layoutView.setPadding(t),tmpl.control.sContent)
  }
}

class StackLayoutUIComp extends LayoutViewUIComp {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val stackLayout = entity.addComponent[StackLayout]()
    setViewParam(stackLayout,dic,tmpl)
    UIComponent.initParam[Float]("spacing",dic,s => stackLayout.setSpacing(s),tmpl.control.sContent)
    UIComponent.initParam[Orientation]("orientation",dic,o => stackLayout.setOrientation(o),tmpl.control.sContent)
  }
}

class ContentViewUIComp extends LayoutViewUIComp {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val contentView = entity.addComponent[ContentView]()
    setViewParam(contentView,dic,tmpl)
  }
}

class GridLayoutUIComp extends LayoutViewUIComp {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val gridLayout = entity.addComponent[GridLayout]()
    setViewParam(gridLayout,dic,tmpl)
    UIComponent.initLispParam[js.Array[LNumber]]("rows",dic,rows => gridLayout.setRows(rows),tmpl.control.sContent)
    UIComponent.initLispParam[js.Array[LNumber]]("cols",dic,cols => gridLayout.setCols(cols),tmpl.control.sContent)
  }
}

class GridCellUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val gridCell = entity.addComponent[GridCell]()
    UIComponent.initParam[Int]("row",dic,row => {gridCell.setRow(row)},tmpl.control.sContent)
    UIComponent.initParam[Int]("col",dic,col => gridCell.setCol(col),tmpl.control.sContent)
    UIComponent.initParam[Int]("rowSpan",dic,rowSpan => gridCell.setRowSpan(rowSpan),tmpl.control.sContent)
    UIComponent.initParam[Int]("colSpan",dic,colSpan => gridCell.setColSpan(colSpan),tmpl.control.sContent)
  }
}

class CABEventRootUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, tmpl: UITemplate): Unit = {
    entity.addComponent[CABEventRoot]()
  }
}