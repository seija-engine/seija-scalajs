package s2d
import math.{Vector2, Vector3}
import core.{BaseComponent, Component, Entity, Foreign, TemplateComponent, TemplateParam, Transform, World}

import scala.scalajs.js

class Rect2D(override val entity:Entity) extends BaseComponent(entity) {
  private var _size:Vector2 = Vector2.defaultByCB(this.sizeToRust);
  private var _anchor:Vector2 = Vector2.defaultByCB(this.anchorToRust,0.5f,0.5f);

  def size:Vector2 = {
    Foreign.getRect2DSizeRef(this.entity.id,_size.inner())
    _size
  }

  def anchor:Vector2 = {
    Foreign.getRect2DAnchorRef(this.entity.id,_anchor.inner())
    _anchor
  }

  def size_= (size:Vector2):Unit = {
    this._size = size;
    this._size.setCallBack(this.sizeToRust)
    this.sizeToRust()
  }

  def anchor_= (vec:Vector2):Unit = {
    this._anchor = vec;
    this._anchor.setCallBack(this.anchorToRust)
    this.anchorToRust()
  }

  private def sizeToRust():Unit = Foreign.setRect2DSizeRef(this.entity.id,this._size.inner())
  private def anchorToRust():Unit = Foreign.setRect2DAnchorRef(this.entity.id,this._anchor.inner())
}


object Rect2D {
  implicit val rect2dComp: Component[Rect2D] = new Component[Rect2D] {
    override def addToEntity(e: Entity): Rect2D = {
      Foreign.addRect2D(e.id);
      new Rect2D(e)
    }
    override val key:String = "Rect2D"
  }
}


class Rect2DTmpl extends TemplateComponent {
  override val name: String = "Rect2D"
  def attachComponent(entity: Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]]):Unit = {
    var rect2d = entity.addComponent[Rect2D]();
    println("attach Rect2D")
    TemplateParam.setValueByAttrDic[Vector2](attrs,"size", rect2d.size = _,data,parentConst)
                 .left.foreach(v => println(s"error Rect2D.size: $v"))

    TemplateParam.setValueByAttrDic[Vector2](attrs,"anchor", rect2d.anchor = _,data,parentConst)
                 .left.foreach(v => println(s"error Rect2D.anchor: $v"))
  }
}