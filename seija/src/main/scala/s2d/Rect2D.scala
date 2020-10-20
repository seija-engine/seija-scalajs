package s2d
import math.Vector2
import core.{BaseComponent, Component, Entity, Foreign, Transform, World}

class Rect2D(override val entity:Entity) extends BaseComponent(entity) {
  private var _size:Vector2 = Vector2.defaultByCB(this.sizeToRust);
  private var _anchor:Vector2 = Vector2.defaultByCB(this.anchorToRust);

  def size:Vector2 = {
    Foreign.getRect2DSizeRef(World.id,this.entity.id,_size.inner())
    _size
  }

  def anchor:Vector2 = {
    Foreign.getRect2DAnchorRef(World.id,this.entity.id,_anchor.inner())
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

  private def sizeToRust():Unit = Foreign.setRect2DSizeRef(World.id,this.entity.id,this._size.inner())
  private def anchorToRust():Unit = Foreign.setRect2DAnchorRef(World.id,this.entity.id,this._anchor.inner())
}


object Rect2D {
  implicit val rect2dComp: Component[Rect2D] = new Component[Rect2D] {
    override def addToEntity(e: Entity): Rect2D = {
      Foreign.addRect2D(World.id,e.id);
      new Rect2D(e)
    }
    override val key:Int = 1
  }
}