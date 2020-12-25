package seija.s2d.layout
import seija.core.{Component, Entity, Foreign}

class ContentView (override val entity:Entity) extends LayoutView(entity) {}

object ContentView {
  implicit val contentViewComp:Component[ContentView] = new Component[ContentView] {
    override def addToEntity(e: Entity): ContentView = {
      Foreign.addContentView(e.id)
      new ContentView(e)
    }

    override val key: String = "ContentView"
  }
}