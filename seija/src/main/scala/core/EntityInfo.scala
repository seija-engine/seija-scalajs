package core

class EntityInfo(val entity:Entity) {
  def name:String = Foreign.getEntityName(World.id,entity.id)
  def name_= (name:String): Unit = Foreign.setEntityName(World.id,entity.id,name)
}
