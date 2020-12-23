package seija.s2d.layout

import seija.core.{BaseComponent, Component, Entity, Foreign}
import seija.s2d.layout.Orientation.Orientation
import slogging.LazyLogging

class StackLayout(override val entity:Entity) extends LayoutView(entity) {
  protected var _spacing:Float = 0
  protected var _orientation:Orientation = Orientation.Horizontal

  def spacing:Float = _spacing
  def setSpacing(f:Float):Unit = {
    _spacing = f
    Foreign.setStackSpacing(entity.id,_spacing)
  }

  def orientation:Orientation = _orientation
  def setOrientation(newVal:Orientation):Unit = {
    _orientation = newVal
    Foreign.setStackOrientation(this.entity.id,_orientation.id)
  }
}

object StackLayout extends LazyLogging {
  implicit val layoutViewComp: Component[StackLayout] = new Component[StackLayout] {
    override val key: String = "StackLayout"
    override def addToEntity(e: Entity): StackLayout = {
      Foreign.addStackLayout(e.id)
      new StackLayout(e)
    }
  }
}