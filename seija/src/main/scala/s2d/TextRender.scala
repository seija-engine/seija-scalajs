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

  def setFont(font:Font):Unit = Foreign.setTextFont(World.id,entity.id,font.id)

  def setText(str:String):Unit = Foreign.setTextString(World.id,entity.id,str)

  def setLineMode(lineMode: LineMode.LinMode):Unit = Foreign.setTextLineMode(World.id,entity.id,lineMode.id)

  def setAnchor(anchor:AnchorAlign):Unit = Foreign.setTextAnchor(World.id,entity.id,anchor.id)

  def setFontSize(size:Int):Unit = Foreign.setTextFontSize(World.id,entity.id,size)

  def colorToRust():Unit = Foreign.setTextColor(World.id,entity.id,_color.inner())
}

object TextRender {
  implicit val textRenderComp: Component[TextRender] = new Component[TextRender] {
    override val key: Int = 4
    override def addToEntity(e: Entity): TextRender = {
      Foreign.addTextRender(World.id,e.id,None)
      new TextRender(e)
    }
  }
}

object LineMode extends Enumeration {
  type LinMode = Value;
  val Single:LinMode = Value(0);
  val Wrap:LinMode = Value(1)
}


class TextRenderTmpl extends TemplateComponent {
  override val name: String = "TextRender"
  def attachComponent(entity: Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any]):Unit = {
    println("attach TextRender");
    val textRender = entity.addComponent[TextRender]()
    val errFont = TemplateParam.setToByAttrDic[Int](attrs,"font", fontId => textRender.setFont(new Font(fontId)),data)
    val errText =TemplateParam.setToByAttrDic[String](attrs,"text",textRender.setText,data);
    val errFontSize =TemplateParam.setToByAttrDic[Int](attrs,"fontSize",textRender.setFontSize,data);



    textRender.color.set(1,1,1,1)
  }
}