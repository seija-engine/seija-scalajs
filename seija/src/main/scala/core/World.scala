package core

object World {
  private var _id:Int = -1;

  def init(id:Int):Unit = {
    this._id = id
  }

  def id:Int = this._id

}
