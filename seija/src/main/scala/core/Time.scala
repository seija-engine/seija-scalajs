package core

object Time {
  def absoluteTime():Float = Foreign.getAbsoluteTime(World.id)

}
