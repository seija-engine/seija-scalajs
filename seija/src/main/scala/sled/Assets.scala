package sled

import seija.assets
import seija.assets.Loader
import seija.s2d.assets.{Font, Image, SpriteSheet}
import slogging.LazyLogging

object Assets extends LazyLogging {
  var font:Option[Font] = None
  var white:Option[Image] = None
  var chromeSheet:Option[SpriteSheet] = None
  def init():Unit = {
    assets.Loader.setAssetRoot("./res/")
    font = Loader.loadSync[Font]("WenQuanYiMicroHei.ttf").toOption
    white = Loader.loadSync[Image]("white.png").toOption
    chromeSheet = Loader.loadSync[SpriteSheet]("ChromeOS.json").toOption
    logger.info(s"load assets $chromeSheet $font $white")
  }
}
