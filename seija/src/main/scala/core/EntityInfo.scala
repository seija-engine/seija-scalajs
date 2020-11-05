package core

class EntityInfo(val entity:Entity) {
  def name:String = Foreign.getEntityName(entity.id)
  def name_= (name:String): Unit = Foreign.setEntityName(entity.id,name)
}
