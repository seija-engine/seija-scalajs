package seija.core

object Time {
  def absoluteTime():Float = Foreign.getAbsoluteTime

  def timeDelta():Float = Foreign.getTimeDelta

  def timeScale():Float = Foreign.getTimeScale
}
