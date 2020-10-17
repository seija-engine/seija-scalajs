package core

trait Component[T] {
   def addToEntity(e:Entity): T
   def key():Int
}