package core

import scala.scalajs.js.typedarray.Float32Array;
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object Foreign {
  def init_deno(): Unit = {
    console.log = (v:js.Any) => {
      Deno.core.print(v.toString +"\n")
    }
    Deno.core.ops();
  }

  def newSimple2d(dict:js.Dictionary[js.Any]):Int = Deno.core.jsonOpSync("newSimple2d",dict).asInstanceOf[Int]

  def getAbsoluteTime(w:Int):Float = Deno.core.jsonOpSync("getAbsoluteTime",w).asInstanceOf[Float]

  def getTimeDelta(world:Int):Float = Deno.core.jsonOpSync("getTimeDelta",world).asInstanceOf[Float]

  def getTimeScale(world:Int):Float = Deno.core.jsonOpSync("getTimeScale",world).asInstanceOf[Float]

  def closeApp(w:Int):Unit = Deno.core.jsonOpSync("closeApp",w)

  def newEntity(w:Int):Int = Deno.core.jsonOpSync("newEntity",w).asInstanceOf[Int]

  def entitySetParent(world: Int,entity:Int,parent:Int):Unit =
    Deno.core.jsonOpSync("entitySetParent",js.Array(world,entity,parent))

  def entityAll(world: Int):js.Array[Int] =
    Deno.core.jsonOpSync("entityAll",world).asInstanceOf[js.Array[Int]]

  def deleteEntity(world: Int,entity: Int):Unit =
    Deno.core.jsonOpSync("deleteEntity",js.Array(world,entity))


  def entityIsAlive(world:Int,entity:Int):Boolean = 
    Deno.core.jsonOpSync("entityIsAlive",js.Array(world,entity)).asInstanceOf[Boolean]

  def addTransform(world:Int,entity:Int):Unit = Deno.core.jsonOpSync("addTransform",js.Array(world,entity))

  def getTransformPosition(world:Int,entity: Int):js.Array[Float] =
    Deno.core.jsonOpSync("getTransformPosition",js.Array(world,entity)).asInstanceOf[js.Array[Float]];

  def setTransformPositionRef(world:Int,entity: Int,pos:Float32Array):Unit =
    Deno.core.jsonOpSync("setTransformPositionRef",js.Array(world,entity),pos)

  def writeTransformPositionRef(world:Int,entity: Int,pos:Float32Array):Unit =
    Deno.core.jsonOpSync("getTransformPositionRef",js.Array(world,entity),pos)

  def writeTransformScaleRef(world:Int,entity: Int,scale:Float32Array):Unit =
    Deno.core.jsonOpSync("getTransformScaleRef",js.Array(world,entity),scale)

  def setTransformScaleRef(world:Int,entity: Int,scale:Float32Array):Unit =
    Deno.core.jsonOpSync("setTransformScaleRef",js.Array(world,entity),scale)

  def writeTransformRotationRef(world: Int,entity: Int,r:Float32Array):Unit =
    Deno.core.jsonOpSync("getTransformRotationRef",js.Array(world,entity),r)

  def setTransformRotationRef(world: Int,entity: Int,r:Float32Array):Unit =
    Deno.core.jsonOpSync("setTransformRotationRef",js.Array(world,entity),r)

  def addRect2D(world: Int,entity: Int):Unit =
    Deno.core.jsonOpSync("addRect2D",js.Array(world,entity,0,0,0,0))

  def setRect2DSizeRef(world: Int,entity: Int,buffer:Float32Array):Unit =
    Deno.core.jsonOpSync("setRect2DSizeRef",js.Array(world,entity),buffer)

  def getRect2DSizeRef(world: Int,entity: Int,buffer:Float32Array):js.Any =
    Deno.core.jsonOpSync("getRect2DSizeRef",js.Array(world,entity),buffer)

  def setRect2DAnchorRef(world: Int,entity: Int,buffer:Float32Array):Unit =
    Deno.core.jsonOpSync("setRect2DAnchorRef",js.Array(world,entity),buffer)

  def getRect2DAnchorRef(world:Int,entity: Int,buffer:Float32Array):js.Any =
    Deno.core.jsonOpSync("getRect2DAnchorRef",js.Array(world,entity),buffer)

  def setTransparent(world: Int,entity:Int,isTransparent:Boolean):Unit =
    Deno.core.jsonOpSync("setTransparent",js.Array(world,entity,isTransparent))
}

@js.native
@JSGlobal
object console extends js.Object {
  var log:js.Any = js.native;
}

@js.native
@JSGlobal
object Deno extends js.Object {
  val core:DenoCore = js.native;
}

@js.native
trait DenoCore extends js.Object {
  def print(v:js.Any):Unit
  def ops():Unit
  def jsonOpSync(name:String,value:js.Any,buffer:js.Any = js.native):js.Any
}

@js.native
@JSGlobal
object Seija extends js.Object {
  def runApp(int: Int,start:js.Function1[Int,Unit],update:js.Function0[Unit],quit:js.Function0[Unit]):Unit = js.native
}

