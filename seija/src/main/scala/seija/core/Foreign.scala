package seija.core

import scala.scalajs.js.typedarray.Float32Array;
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

trait ToJSValue {
  def toJsValue:js.Any
}

object Foreign {
  def initDeno(): Unit = {
    console.log = (v:js.Any) => {
      Deno.core.print(v.toString +"\n")
    }
    console.error = (v:js.Any) => {
      Deno.core.print(v.toString +"\n",1)
    }
    console.warn = (v:js.Any) => {
      Deno.core.print(v.toString +"\n")
    }
    Deno.core.ops();
  }

  def newSimple2d(dict:js.Dictionary[js.Any]):Int = Deno.core.jsonOpSync("newSimple2d",dict).asInstanceOf[Int]

  def getAbsoluteTime:Float = Deno.core.jsonOpSync("getAbsoluteTime").asInstanceOf[Float]

  def getTimeDelta:Float = Deno.core.jsonOpSync("getTimeDelta").asInstanceOf[Float]

  def getTimeScale:Float = Deno.core.jsonOpSync("getTimeScale").asInstanceOf[Float]

  def getTimeFrame:Int = Deno.core.jsonOpSync("getTimeFrame").asInstanceOf[Int]

  def closeApp():Unit = Deno.core.jsonOpSync("closeApp")

  def newEntity:Int = Deno.core.jsonOpSync("newEntity").asInstanceOf[Int]

  def entityAll:js.Array[Int] =
    Deno.core.jsonOpSync("entityAll").asInstanceOf[js.Array[Int]]

  def deleteEntity(entity: Int):Unit =
    Deno.core.jsonOpSync("deleteEntity",entity)

  def treeAdd(entity: Int,parent:Option[Int]): Int = {
    val arr:js.Array[Any] = js.Array(entity,parent.orNull)
    Deno.core.jsonOpSync("treeAdd",arr).asInstanceOf[Int]
  }

  def treeUpdate(entity: Int,parent:Option[Int]):Int = {
    val arr:js.Array[Any] = js.Array(entity,parent.orNull)
    Deno.core.jsonOpSync("treeUpdate",arr).asInstanceOf[Int]
  }

  def treeRemove(entity: Int, isDestroy:Boolean):Unit =
    Deno.core.jsonOpSync("treeRemove",js.Array(entity,isDestroy))

  def entityIsAlive(entity:Int):Boolean =
    Deno.core.jsonOpSync("entityIsAlive",entity).asInstanceOf[Boolean]

  def addTransform(entity:Int):Unit = Deno.core.jsonOpSync("addTransform",entity)

  def getTransformPosition(entity: Int):js.Array[Float] =
    Deno.core.jsonOpSync("getTransformPosition",entity).asInstanceOf[js.Array[Float]]

  def setTransformPositionRef(entity: Int,pos:Float32Array):Unit =
    Deno.core.jsonOpSync("setTransformPositionRef",entity,pos)

  def writeTransformPositionRef(entity: Int,pos:Float32Array):Unit =
    Deno.core.jsonOpSync("getTransformPositionRef",entity,pos)

  def writeTransformScaleRef(entity: Int,scale:Float32Array):Unit =
    Deno.core.jsonOpSync("getTransformScaleRef",entity,scale)

  def setTransformScaleRef(entity: Int,scale:Float32Array):Unit =
    Deno.core.jsonOpSync("setTransformScaleRef",entity,scale)

  def writeTransformRotationRef(entity: Int,r:Float32Array):Unit =
    Deno.core.jsonOpSync("getTransformRotationRef",entity,r)

  def setTransformRotationRef(entity: Int,r:Float32Array):Unit =
    Deno.core.jsonOpSync("setTransformRotationRef",entity,r)

  def addRect2D(entity: Int):Unit =
    Deno.core.jsonOpSync("addRect2D",js.Array(entity,0,0,0.5,0.5))

  def setRect2DSizeRef(entity: Int,buffer:Float32Array):Unit =
    Deno.core.jsonOpSync("setRect2DSizeRef",entity,buffer)

  def getRect2DSizeRef(entity: Int,buffer:Float32Array):js.Any =
    Deno.core.jsonOpSync("getRect2DSizeRef",entity,buffer)

  def setRect2DAnchorRef(entity: Int,buffer:Float32Array):Unit =
    Deno.core.jsonOpSync("setRect2DAnchorRef",entity,buffer)

  def getRect2DAnchorRef(entity: Int,buffer:Float32Array):js.Any =
    Deno.core.jsonOpSync("getRect2DAnchorRef",entity,buffer)

  def setTransparent(entity:Int,isTransparent:Boolean):Unit =
    Deno.core.jsonOpSync("setTransparent",js.Array(entity,isTransparent))

  def loadSync(path:String,assetType:Int,config:js.Any):js.Any =
    Deno.core.jsonOpSync("loadSync",js.Array(assetType,path,config))

  def setAssetRootPath(path:String):Unit =
    Deno.core.jsonOpSync("setAssetRootPath",path)

  def addImageRender(entity: Int,textureId:Option[Int]):Unit =
    Deno.core.jsonOpSync("addImageRender",js.Array(entity,textureId))

  def setImageTexture(entity: Int,textureId:Int):Unit =
    Deno.core.jsonOpSync("setImageTexture",js.Array(entity,textureId))

  def getImageColor(entity:Int,buffer:Float32Array):js.Any =
    Deno.core.jsonOpSync("getImageColorRef",entity,buffer)

  def setImageColor(entity:Int,buffer:Float32Array):Unit =
    Deno.core.jsonOpSync("setImageColorRef",entity,buffer)

  def addSpriteRender(entity: Int):Unit =
    Deno.core.jsonOpSync("addSpriteRender",js.Array(entity,null,null))

  def setSpriteSheet(entity: Int,sheet:Int):Unit =
    Deno.core.jsonOpSync("setSpriteSheet",js.Array(entity,sheet))

  def setSpriteName(entity: Int,name:String):Unit =
    Deno.core.jsonOpSync("setSpriteName",js.Array(entity,name))

  def setSpriteColor(entity:Int,buffer:Float32Array):Unit = {
    Deno.core.jsonOpSync("setSpriteColorRef",entity,buffer)
  }

  def getSpriteColor(entity: Int,buffer:Float32Array):js.Any =
    Deno.core.jsonOpSync("getSpriteColorRef",entity,buffer)

  def setImageType(entity: Int,value:js.Any):Unit =
    Deno.core.jsonOpSync("setImageType",js.Array(entity,value))

  def setSpriteType(entity: Int,value:js.Any):Unit =
    Deno.core.jsonOpSync("setSpriteType",js.Array(entity,value))

  def setImageFilledValue(entity: Int,value:Float):Unit =
    Deno.core.jsonOpSync("setImageFilledValue",js.Array(entity,value))

  def setSpriteFilledValue(entity: Int,value:Float):Unit =
    Deno.core.jsonOpSync("setSpriteFilledValue",js.Array(entity,value))

  def setSpriteSliceByConfig(entity: Int,value:Int):Unit =
    Deno.core.jsonOpSync("setSpriteSliceByConfig",js.Array(entity,value))

  def addTextRender(entity: Int,fontId:Option[Int]):Unit =
    Deno.core.jsonOpSync("addTextRender",js.Array(entity,fontId.getOrElse(false)))

  def setTextFont(entity: Int,fontId:Int):Unit =
    Deno.core.jsonOpSync("setTextFont",js.Array(entity,fontId))

  def setTextString(entity: Int,str:String):Unit =
    Deno.core.jsonOpSync("setTextString",js.Array(entity,str))

  def setTextFontSize(entity: Int,fontSize:Int):Unit =
    Deno.core.jsonOpSync("setTextFontSize",js.Array(entity,fontSize))

  def setTextColor(entity: Int,color:Float32Array):Unit =
    Deno.core.jsonOpSync("setTextColorRef",entity,color)

  def setTextLineMode(entity:Int,lineMode:Int):Unit =
    Deno.core.jsonOpSync("setTextLineMode",js.Array(entity,lineMode))

  def setTextAnchor(entity: Int,anchorType:Int):Unit =
    Deno.core.jsonOpSync("setTextAnchor",js.Array(entity,anchorType))

  def addEntityInfo(entity: Int,name:String):Unit =
    Deno.core.jsonOpSync("addEntityInfo",js.Array(entity,name))

  def setEntityName(entity:Int,name:String):Unit =
    Deno.core.jsonOpSync("setEntityName",js.Array(entity,name))

  def getEntityName(entity:Int):String =
    Deno.core.jsonOpSync("getEntityName",entity).asInstanceOf[String]

  def addCABEventRoot(entity:Int):Unit =
    Deno.core.jsonOpSync("addCABEventRoot",entity)

  def addEventNode(entity:Int):Boolean =
    Deno.core.jsonOpSync("addEventNode",js.Array(entity)).asInstanceOf[Boolean]

  def setEventThrough(entity:Int,b:Boolean) = 
    Deno.core.jsonOpSync("setEventThrough",js.Array(entity,b))
  
  def regEventNodeEvent(entity:Int,evType:Int,isCapture:Boolean): Boolean =
    Deno.core.jsonOpSync("addEventNode",js.Array(entity,evType,isCapture)).asInstanceOf[Boolean]

  def addLayoutView(entity: Int):Boolean =
    Deno.core.jsonOpSync("addLayoutView",entity).asInstanceOf[Boolean]

  def setLayoutMargin(entity: Int,l:Float,t:Float,r:Float,b:Float):Unit =
    Deno.core.jsonOpSync("setLayoutMargin",js.Array(entity,l,t,r,b))
  
  def setLayoutPadding(entity:Int,l:Float,t:Float,r:Float,b:Float):Unit = 
   Deno.core.jsonOpSync("setLayoutPadding",js.Array(entity,l,t,r,b))

  def setLayoutHor(entity: Int,typ:Int):Unit =
    Deno.core.jsonOpSync("setLayoutHor",js.Array(entity,typ))

  def setLayoutVer(entity: Int,typ:Int):Unit =
    Deno.core.jsonOpSync("setLayoutVer",js.Array(entity,typ))

  def setLayoutSize(entity: Int,w:Float,h:Float): Unit =
    Deno.core.jsonOpSync("setLayoutSize",js.Array(entity,w,h))

  def setLayoutViewType(entity:Int,typ:Int):Unit =
    Deno.core.jsonOpSync("setLayoutViewType",js.Array(entity,typ))

  def setLayoutPosition(entity: Int,x:Float,y:Float,z:Float): Unit =
    Deno.core.jsonOpSync("setLayoutPosition",js.Array(entity,x,y,z))

  def addStackLayout(entity: Int):Boolean =
    Deno.core.jsonOpSync("addStackLayout",entity).asInstanceOf[Boolean]

  def setStackSpacing(entity: Int,f:Float):Unit =
    Deno.core.jsonOpSync("setStackSpacing",js.Array(entity,f))

  def setStackOrientation(entity: Int,t:Int):Unit =
    Deno.core.jsonOpSync("setStackOrientation",js.Array(entity,t))

  def addGridLayout(entity: Int):Boolean =
    Deno.core.jsonOpSync("addGridLayout",entity).asInstanceOf[Boolean]

  def addGridRow(entity: Int,t:Int,number:Float):Unit =
    Deno.core.jsonOpSync("addGridRow",js.Array(entity,t,number))

  def addGridCol(entity:Int,t:Int,number:Float):Unit =
    Deno.core.jsonOpSync("addGridCol",js.Array(entity,t,number))

  def setGridRows(entity:Int, arrays: js.Array[Any]):Unit =
    Deno.core.jsonOpSync("setGridRows",js.Array(entity,arrays))

  def setGridCols(entity:Int, arrays: js.Array[Any]):Unit = {
    Deno.core.jsonOpSync("setGridCols",js.Array(entity,arrays))
  }

  def addGridCell(entity:Int):Boolean =
    Deno.core.jsonOpSync("addGridCell",entity).asInstanceOf[Boolean]

  def setGridCell(entity:Int,row:Int,col:Int,rowSpan:Int,colSpan:Int):Unit = {
    Deno.core.jsonOpSync("setGridCell",js.Array(entity,row,col,rowSpan,colSpan))
  }

  def addContentView(entity:Int):Boolean =
    Deno.core.jsonOpSync("addContentView",entity).asInstanceOf[Boolean]

  def removeContentView(entity:Int):Unit =
    Deno.core.jsonOpSync("removeContentView",entity)
}

@js.native
@JSGlobal
object console extends js.Object {
  var log:js.Any = js.native
  var error:js.Any = js.native
  var warn:js.Any = js.native
}

@js.native
@JSGlobal
object Deno extends js.Object {
  val core:DenoCore = js.native;
}

@js.native
trait DenoCore extends js.Object {
  def print(v:js.Any*):Unit
  def ops():Unit
  def jsonOpSync(name:String,value:js.Any = null,buffer:js.Any = js.native):js.Any
}

@js.native
@JSGlobal
object Seija extends js.Object {
  def runApp(int: Int,start:js.Function1[Int,Unit],update:js.Function,quit:js.Function0[Unit]):Unit = js.native

  def parseXML(path:String):js.Any = js.native

  def parseXMLString(string:String):js.Any = js.native
}

