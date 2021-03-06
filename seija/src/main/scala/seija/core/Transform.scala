package seija.core

import seija.math.Vector3

import scala.scalajs.js

class Transform(override val entity:Entity) extends BaseComponent(entity) {
  private var _localPosition:Vector3 = Vector3.defaultByCB(this.positionToRust)
  private var _scale:Vector3 = Vector3.defaultByCB(this.scaleToRust)
  private var _rotation:Vector3 = Vector3.defaultByCB(this.rotationToRust)


  def localPosition:Vector3 = {
    Foreign.writeTransformPositionRef(this.entity.id,this._localPosition.inner())
    this._localPosition
  }

  def globalPosition:Vector3 = {
    val posArr = Foreign.getTransformGlobalPostion(this.entity.id);
    Vector3.New(posArr(0),posArr(1),posArr(2))
  }

  def localPosition_= (newVal:Vector3): Unit = {
    this._localPosition = newVal
    newVal.setCallBack(this.positionToRust);
    this.positionToRust();
  }

  def scale:Vector3 = {
    Foreign.writeTransformScaleRef(this.entity.id,this._scale.inner())
    this._scale
  }

  def scale_= (newScale:Vector3):Unit = {
    this._scale = newScale
    this._scale.setCallBack(this.scaleToRust)
    this.scaleToRust()
  }

  def rotation:Vector3 = {
    Foreign.writeTransformRotationRef(this.entity.id,this._rotation.inner())
    this._rotation
  }

  def rotation_= (rotation:Vector3):Unit = {
    this._rotation = rotation
    this._rotation.setCallBack(this.rotationToRust);
    this.rotationToRust();
  }

  private def positionToRust():Unit = Foreign.setTransformPositionRef(this.entity.id,this._localPosition.inner());
  private def scaleToRust():Unit = Foreign.setTransformScaleRef(this.entity.id,this._scale.inner())
  private def rotationToRust():Unit = Foreign.setTransformRotationRef(this.entity.id,this._rotation.inner())
}

object Transform {
  implicit val transformComp: Component[Transform] = new Component[Transform] {
    override def addToEntity(e: Entity): Transform = {
      Foreign.addTransform(e.id);
      new Transform(e)
    }
    override val key:String = "Transform"
  }
}


class TransformTmpl extends TemplateComponent {
  override val name: String = "Transform"
  def attachComponent(entity: Entity,attrs:js.Dictionary[String],data:js.Dictionary[Any],parentConst:Option[js.Dictionary[String]]):Unit = {
    val trans = entity.addComponent[Transform]();
    TemplateParam.setValueByAttrDic[Vector3](attrs,"position", trans.localPosition = _,data,parentConst)
                 .left.foreach(v => println(s"Transform.position error: $v"))
    TemplateParam.setValueByAttrDic[Vector3](attrs,"scale", trans.scale = _,data,parentConst)
                 .left.foreach(v => println(s"Transform.scale error: $v"))
    TemplateParam.setValueByAttrDic[Vector3](attrs,"rotation", trans.rotation = _,data,parentConst)
                 .left.foreach(v => println(s"Transfrom.rotation error: $v"))

  }
}