package seija.ui2
import scala.scalajs.js
import seija.core.event.{CABEventRoot, EventNode, GameEventType}
import seija.core.{Entity, Transform}
import seija.data.{Color, SBool, SExprInterp, SFunc, SList, SNil, SUserData, SVector, XmlNode}
import seija.math.{Vector2, Vector3}
import seija.s2d.{ImageRender, ImageType, Rect2D, SpriteRender, TextRender, Transparent}
import seija.s2d.assets.{Image, SpriteSheet}
import seija.data.Read._
import seija.s2d.assets.Font
import seija.s2d.layout.LayoutAlignment.LayoutAlignment
import seija.s2d.layout.Orientation.Orientation
import seija.s2d.layout.ViewType.ViewType
import seija.s2d.layout.{ContentView, GridCell, GridLayout, LNumber, LayoutView, StackLayout, Thickness}
import seija.data.SContent

class TransformUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode,control:Control): Unit = {
    val trans = entity.addComponent[Transform]()
    val dic = Utils.getXmlStringParam(xmlNode)
    UIComponent.initParam[Vector3]("position",dic,trans.localPosition = _,control.sContent);
    UIComponent.initParam[Vector3]("scale",dic,trans.scale = _,control.sContent);
    UIComponent.initParam[Vector3]("rotation",dic,trans.rotation = _,control.sContent);
  }
}

class Rect2DUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,control:Control): Unit = {
    val rect2d = entity.addComponent[Rect2D]()
    val dic = Utils.getXmlStringParam(xmlNode)
    UIComponent.initParam[Vector2]("size",dic,rect2d.size = _,control.sContent)
    UIComponent.initParam[Vector2]("anchor",dic,rect2d.anchor = _,control.sContent)
  }
}

class ImageRenderUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,control:Control): Unit = {
    val image = entity.addComponent[ImageRender]()
    val dic = Utils.getXmlStringParam(xmlNode)
    UIComponent.initParam[Int]("texture",dic,tex => image.setTexture(new Image(tex)),control.sContent)
    UIComponent.initParam[Color]("color",dic,color => {
      image.color = color
    },control.sContent)
  }
}

class SpriteRenderUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, control: Control): Unit = {
    val sprite = entity.addComponent[SpriteRender]()
    val dic = Utils.getXmlStringParam(xmlNode)
    UIComponent.initParam[String]("spriteName",dic,sName => sprite.setSpriteName(sName),control.sContent)
    UIComponent.initParam[Color]("color",dic,sprite.color = _,control.sContent)
    UIComponent.initParam[Int]("sheet",dic,sheet => sprite.setSpriteSheet(new SpriteSheet(sheet)),control.sContent)
    UIComponent.initParam[ImageType]("type",dic,t => sprite.setImageType(t),control.sContent)
  }
}

class TransparentUIComp extends UIComponent {
  override def attach(entity: Entity,xmlNode: XmlNode,control:Control): Unit = {
    entity.addComponent[Transparent]()
  }
}

class EventNodeUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, control: Control): Unit = {
    val eventNode = entity.addComponent[EventNode]()
    val dic = Utils.getXmlStringParam(xmlNode)

    for((k,v) <- dic) {
      val (head,tail) = k.splitAt(2)
      if(head == "On") {
        val evType = GameEventType.gameEventTypeRead.read(tail)
        if(evType.isDefined) {
          UIComponent.cacheContent.clear()
          UIComponent.cacheContent.parent = Some(control.sContent)
          UIComponent.cacheContent.set("event-node",SUserData(eventNode))
          val evalValue = SExprInterp.evalString(v, Some(UIComponent.cacheContent))
         
          evalValue match {
            case Right(SVector(list)) =>
              val isCapture = list(0).asInstanceOf[SBool].value
              val f = list(1).asInstanceOf[SFunc]
              eventNode.register(evType.get,isCapture,() => {
                f.call(Some(control.sContent))
              })
            case Right(f@SFunc(args, list)) =>
              eventNode.register(evType.get,isCapture = false, () => {
                f.call(Some(control.sContent))
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
  override def attach(entity: Entity, xmlNode: XmlNode, control: Control): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val eventBoard = entity.addComponent[EventBoardComponent]()
    eventBoard.initBoard(dic.get("name").getOrElse(""))
    control.eventBoard = eventBoard.eventBoard
  }
}

class TextRenderUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, control: Control): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val textRender = entity.addComponent[TextRender]()
    UIComponent.initParam[String]("text",dic,sText =>textRender.setText(sText),control.sContent)
    UIComponent.initParam[Color]("color",dic,textRender.color = _,control.sContent)
    UIComponent.initParam[Int]("font",dic,fontID => textRender.setFont(new Font(fontID)) ,control.sContent)
    UIComponent.initParam[Int]("fontSize",dic,fontSize => textRender.setFontSize(fontSize),control.sContent)
  }
}

class LayoutViewUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, control: Control): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val layoutView = entity.addComponent[LayoutView]()
    setViewParam(layoutView,dic,control.sContent)
  }

  def setViewParam(layoutView:LayoutView,dic:js.Dictionary[String], content: SContent): Unit = {
    UIComponent.initParam[LayoutAlignment]("hor",dic, hor => layoutView.setHor(hor),content)
    UIComponent.initParam[LayoutAlignment]("ver",dic,ver => layoutView.setVer(ver),content)
    UIComponent.initParam[Vector2]("size",dic,s => {
      layoutView.setSize(s)
    },content)
    UIComponent.initParam[Vector3]("position",dic,s => layoutView.setPosition(s),content)
    UIComponent.initParam[Thickness]("margin",dic,t => layoutView.setMargin(t),content)
    UIComponent.initParam[Thickness]("padding",dic,t => layoutView.setPadding(t),content)
    UIComponent.initParam[ViewType]("viewType",dic,t => layoutView.setViewType(t),content)
  }
}

class StackLayoutUIComp extends LayoutViewUIComp {
  override def attach(entity: Entity, xmlNode: XmlNode, control: Control): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val stackLayout = entity.addComponent[StackLayout]()
    setViewParam(stackLayout,dic,control.sContent)
    UIComponent.initParam[Float]("spacing",dic,s => stackLayout.setSpacing(s),control.sContent)
    UIComponent.initParam[Orientation]("orientation",dic,o => stackLayout.setOrientation(o),control.sContent)
  }
}

class ContentViewUIComp extends LayoutViewUIComp {
  override def attach(entity: Entity, xmlNode: XmlNode, control: Control): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val contentView = entity.addComponent[ContentView]()
    setViewParam(contentView,dic,control.sContent)
  }
}

class GridLayoutUIComp extends LayoutViewUIComp {
  override def attach(entity: Entity, xmlNode: XmlNode, control: Control): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val gridLayout = entity.addComponent[GridLayout]()
    setViewParam(gridLayout,dic,control.sContent)
    UIComponent.initLispParam[js.Array[LNumber]]("rows",dic,rows => gridLayout.setRows(rows),control.sContent)
    UIComponent.initLispParam[js.Array[LNumber]]("cols",dic,cols => gridLayout.setCols(cols),control.sContent)
  }
}

class GridCellUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, control: Control): Unit = {
    val dic = Utils.getXmlStringParam(xmlNode)
    val gridCell = entity.addComponent[GridCell]()
    UIComponent.initParam[Int]("row",dic,row => {gridCell.setRow(row)},control.sContent)
    UIComponent.initParam[Int]("col",dic,col => gridCell.setCol(col),control.sContent)
    UIComponent.initParam[Int]("rowSpan",dic,rowSpan => gridCell.setRowSpan(rowSpan),control.sContent)
    UIComponent.initParam[Int]("colSpan",dic,colSpan => gridCell.setColSpan(colSpan),control.sContent)
  }
}

class CABEventRootUIComp extends UIComponent {
  override def attach(entity: Entity, xmlNode: XmlNode, control: Control): Unit = {
    entity.addComponent[CABEventRoot]()
  }
}