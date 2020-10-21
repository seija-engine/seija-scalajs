package s2d
import data.Color

import scalajs.js;

class SWindow(var width:Int = 1024,
              var height:Int = 768,
              var title:String = "Seija",
              var bgColor:Color = Color.New(1f,1f,1f,1f)) {

}
class Simple2d(var window:SWindow = new SWindow()) {

  def toJS:js.Dictionary[js.Any] = {
    js.Dictionary(
      "window" -> js.Dictionary(
        "width" -> this.window.width,
        "height" -> this.window.height,
        "title" -> this.window.title,
        "bg_color" -> this.window.bgColor.toJsArray,
      )
    )
  }
}