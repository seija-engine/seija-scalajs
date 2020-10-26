package core
import scala.scalajs.js;
import math.Vector3

class Transform(override val entity:Entity) extends BaseComponent(entity) {
  private var _localPosition:Vector3 = Vector3.defaultByCB(this.positonToRust)
  private var _scale:Vector3 = Vector3.defaultByCB(this.scaleToRust)
  private var _rotation:Vector3 = Vector3.defaultByCB(this.rotationToRust)


  def localPosition:Vector3 = {
    Foreign.writeTransformPositionRef(World.id,this.entity.id,this._localPosition.inner())
    this._localPosition
  }

  def localPosition_= (newVal:Vector3): Unit = {
    this._localPosition = newVal
    newVal.setCallBack(this.positonToRust);
    this.positonToRust();
  }

  def scale:Vector3 = {
    Foreign.writeTransformScaleRef(World.id,this.entity.id,this._scale.inner())
    this._scale
  }

  def scale_= (newScale:Vector3):Unit = {
    this._scale = newScale
    this._scale.setCallBack(this.scaleToRust)
    this.scaleToRust()
  }

  def rotation:Vector3 = {
    Foreign.writeTransformRotationRef(World.id,this.entity.id,this._rotation.inner())
    this._rotation
  }

  def rotation_= (rotation:Vector3):Unit = {
    this._rotation = rotation
    this._rotation.setCallBack(this.rotationToRust);
    this.rotationToRust();
  }

  private def positonToRust():Unit = Foreign.setTransformPositionRef(World.id,this.entity.id,this._localPosition.inner());
  private def scaleToRust():Unit = Foreign.setTransformScaleRef(World.id,this.entity.id,this._scale.inner())
  private def rotationToRust():Unit = Foreign.setTransformRotationRef(World.id,this.entity.id,this._rotation.inner())
}

object Transform {
  implicit val transformComp: Component[Transform] = new Component[Transform] {
    override def addToEntity(e: Entity): Transform = {
      Foreign.addTransform(World.id,e.id);
      new Transform(e)
    }
    override val key:Int = 0
  }
}


class TransformTmpl extends TemplateComponent {
  override val name: String = "Transform"
  def attachComponent(entity: Entity,attrs:js.Dictionary[String]):Unit = {
    
  }
}