package seija.data

trait Read[T] {
   def read(string:String):Option[T]
}

object CoreRead {
   implicit val stringRead: Read[String] = (string: String) => Some(string)
   implicit val intRead: Read[Int] = (string: String) => string.toIntOption
   implicit val floatRead:Read[Float] = (string:String) => string.toFloatOption
   implicit val boolRead:Read[Boolean] = (string:String) => string.toBooleanOption
}