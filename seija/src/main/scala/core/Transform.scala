package core

class Transform(private val entity:Entity) {
  def localPosition():Unit = {

  }
}

object Transform {
  implicit val transformComp: Component[Transform] = new Component[Transform] {
    override def addToEntity(e: Entity): Transform = {
      new Transform(e)
    }
    override def key(): Int = 0
  }
}