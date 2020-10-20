package core

trait Component[T] {
   def addToEntity(e:Entity): T
   val key:Int
}

class BaseComponent(protected val entity: Entity) {
 
   def onAttach():Unit = {}

   def onDetach():Unit = {}

}