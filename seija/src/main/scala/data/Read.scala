package data

trait Read[T] {
   def read(string:String):Option[T]
}

object CoreRead {
   implicit val stringRead: data.Read[String] = (string: String) => Some(string)
   implicit val intRead: data.Read[Int] = (string: String) => string.toIntOption
   implicit val floatRead:data.Read[Float] = (string:String) => string.toFloatOption
}