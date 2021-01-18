package seija.s2d.layout
import seija.core.{BaseComponent, Component, Entity, Foreign}
import seija.math.{Vector2, Vector3}
import seija.s2d.layout.LayoutAlignment.LayoutAlignment
import seija.s2d.layout.ViewType.ViewType


class LayoutView (override val entity:Entity) extends BaseComponent(entity) {
  protected var _margin:Thickness = new Thickness(0)
  protected var _padding:Thickness = new Thickness(0)
  protected var _hor:LayoutAlignment = LayoutAlignment.Fill
  protected var _ver:LayoutAlignment = LayoutAlignment.Fill
  protected var _size:Vector2 = Vector2.default()
  protected var _position:Vector2 = Vector2.default()
  protected var _viewType:ViewType = ViewType.Static

  def viewType:ViewType = _viewType
  def setViewType(typ:ViewType): Unit = {
    _viewType = typ
    Foreign.setLayoutViewType(this.entity.id,_viewType.id)
  }
  def margin():Thickness = _margin
  def setMargin(margin:Thickness):Unit = {
    _margin = margin
    this.updateMargin()
  }

  def padding():Thickness = _padding
  def setPadding(padding:Thickness):Unit = {
    _padding = padding
    this.updatePadding()
  }

  def hor:LayoutAlignment = _hor
  def setHor(hor:LayoutAlignment):Unit = {
    _hor = hor
    updateHor()
  }

  def ver:LayoutAlignment = _ver
  def setVer(ver:LayoutAlignment):Unit = {
    _ver = ver
    updateVer()
  }

  def size:Vector2 = _size
  def setSize(size:Vector2):Unit = {
    _size = size
    updateSize()
  }

  def position:Vector2 = _position
  def setPosition(pos:Vector2):Unit = {
    _position = pos
    updatePosition()
  }

  protected def updateMargin():Unit = Foreign.setLayoutMargin(entity.id,_margin.left,_margin.top,_margin.right,_margin.bottom)
  protected def updatePadding():Unit = Foreign.setLayoutPadding(entity.id,_padding.left,_padding.top,_padding.right,_padding.bottom)
  protected def updateHor():Unit = Foreign.setLayoutHor(entity.id,_hor.id)
  protected def updateVer():Unit = Foreign.setLayoutVer(entity.id,_ver.id)
  protected def updateSize():Unit = Foreign.setLayoutSize(entity.id,_size.x,_size.y)
  protected def updatePosition():Unit = Foreign.setLayoutPosition(entity.id,_position.x,_position.y)
}


object LayoutView {
  implicit val layoutViewComp: Component[LayoutView] = new Component[LayoutView] {
    override val key: String = "LayoutView"
    override def addToEntity(e: Entity): LayoutView = {
      Foreign.addLayoutView(e.id)
      new LayoutView(e)
    }
  }

}