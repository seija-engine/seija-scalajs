package core

object Time {
  def absoluteTime():Float = Foreign.getAbsoluteTime(World.id)

  def timeDelta():Float = Foreign.getTimeDelta(World.id)

  def timeScale():Float = Foreign.getTimeScale(World.id)
}
