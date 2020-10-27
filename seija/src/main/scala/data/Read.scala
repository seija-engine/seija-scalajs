package data

trait Read[T] {
   def read(string:String):Option[T]
}