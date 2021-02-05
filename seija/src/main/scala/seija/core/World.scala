package seija.core
import scalajs.js
object World {
  private var _id:js.Object = null;

  def init(id:js.Object):Unit = {
    this._id = id
  }

  def id:js.Object = this._id

}
