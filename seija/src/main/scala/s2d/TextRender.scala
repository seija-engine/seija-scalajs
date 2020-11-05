package s2d

import core.{BaseComponent, Component, Entity, Foreign, TemplateComponent, TemplateParam, World}
import data.AnchorAlign.AnchorAlign
import data.Color
import s2d.assets.Font
import data.CoreRead._

import scala.scalajs.js

class TextRender(override val entity:Entity) extends BaseComponent(entity) {
  private var _color:Color = Color.NewCB(1,1,1,1,this.colorToRust)

  def color:Color = _color

  def color_= (newColor:Color):Unit = {
    _color = newColor
    this.colorToRust()
    _color.setCallback(this.colorToRust)
  }

  def setFont(font:Font):Unit = Foreign.setTextFont(entity.id,font.id)

  def setText(str:String):Unit = Foreign.setTextString(entity.id,str)

  def setLineMode(lineMode: LineMode.LineMode):Unit = Foreign.setTextLineMode(entity.id,lineMode.id)

  def setAnchor(anchor:AnchorAlign):Unit = Foreign.setTextAnchor(entity.id,anchor.id)

  def setFontSize(size:Int):Unit = Foreign.setTextFontSize(entity.id,size)

  def colorToRust():Unit = Foreign.setTextColor(entity.id,_color.inner())
}

object TextRender {
  implicit val textRenderComp: Component[TextRender] = new Component[TextRender] {
    override val key: String = "TextRender"
    override def addToEntity(e: Entity): TextRender = {
      Foreign.addTextRender(e.id,None)
      new TextRender(e)
    }
  }
}

object LineMode extends Enumeration {
  type LineMode = Value;
  val Single:LineMode = Value(0);
  val Wrap:LineMode = Value(1)

  implicit val lineModeRead: data.Read[LineMode] = {
    case "Wrap" => Some(LineMode.Wrap)
    case "Single" => Some(LineMode.Single)
    case _ => None
  }
}


class TextRenderTmpl extends TemplateComponent {
  override val name: String = "TextRender"
  def attachComponent(entity: Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]]):Unit = {
    println("attach TextRender");
    val textRender = entity.addComponent[TextRender]()
    TemplateParam.setValueByAttrDic[Int](attrs,"font", fontId => textRender.setFont(new Font(fontId)),data,parentConst)
                 .left.foreach(v => println(s"error TextRender.font:$v"))
    TemplateParam.setValueByAttrDic[String](attrs,"text",textRender.setText,data,parentConst)
                 .left.foreach(v => println(s"error TextRender.text:$v"))
    TemplateParam.setValueByAttrDic[Int](attrs,"fontSize",textRender.setFontSize,data,parentConst)
                  .left.foreach(v => println(s"error TextRender.fontSize:$v"))
    TemplateParam.setValueByAttrDic[Color](attrs,"color",textRender.color = _,data,parentConst)
                  .left.foreach(v => println(s"error TextRender.color:$v"))
    TemplateParam.setValueByAttrDic[LineMode.LineMode](attrs,"lineMode",textRender.setLineMode,data,parentConst)
                  .left.foreach(v => println(s"error TextRender.lineMode:$v"))
    TemplateParam.setValueByAttrDic[AnchorAlign](attrs,"anchor",textRender.setAnchor,data,parentConst)
                  .left.foreach(v => println(s"error TextRender.anchor:$v"))


  }
}