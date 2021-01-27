package seija.ui
import seija.core.Entity
import seija.core.Transform
import seija.s2d.Rect2D
import seija.core.event.EventNode
import seija.s2d.layout.ContentView
import scalajs.js
import slogging.LazyLogging

case class UILayer(name:String,index:Int) extends LazyLogging {
  var entity:Option[Entity] = None
  var children:js.Array[Control] = js.Array()
  var curDirty:Option[Control] = None

  def addControl(control:Control) {
    this.children.push(control)
    control.entity.map(_.setParent(this.entity))
    this.DiffUpdateDirty(control)
  }

  def removeControl(control:Control) {
    val idx = this.children.indexOf(control)
    if(idx > 0) {
      control.entity.map(_.setParent(None))
      this.children.remove(idx)
    }
  }

  def OnRemoveControl(control:Control) {
    if(this.curDirty.isDefined && this.curDirty.get == control) {
      this.curDirty = None
    }
  }

  def DiffUpdateDirty(newDirty:Control) {
    if(this.curDirty.isEmpty) {
      this.curDirty = Some(newDirty)
      return
    }
    val newSorts = newDirty.getChildSort();
    val oldSorts = this.curDirty.get.getChildSort();
    val diffCount = math.min(oldSorts.length,newSorts.length)
    //logger.info(newSorts.toString())
    for(idx <- 0 until diffCount) {
      if(newSorts(idx) < oldSorts(idx)) {
         this.curDirty = Some(newDirty)
         return
      }
    }
    if(newSorts.length < oldSorts.length) {
      this.curDirty = Some(newDirty)
    }
  }

  def UpdateDirtyZOrder() {
    if(this.curDirty.isEmpty) return;
    var curZIndex = this.findLastZIndex()
    var childList = this.curDirty.get.getChildSort()
    var curControl = this.curDirty
    if(childList.length == 1 && childList(0) == -1) {
       depSetZIndex(curZIndex,this.curDirty.get)
       this.curDirty = None;
       return;
    }
    while (childList.length > 0) {
      var curIndex = childList.pop()
      if(curIndex >= 0 && curIndex < curControl.get.childCount) {
            for(idx <- curIndex to curControl.get.childCount - 1) {
              curZIndex = depSetZIndex(curZIndex,curControl.get.getChild(idx))
            }
      }
      curControl = curControl.flatMap(_.parent)
    }
    this.curDirty = None;
  }

  def depSetZIndex(startZ:Int,control:Control):Int = {
    var curZ = startZ + 1
    control.zIndex = curZ
    //logger.info(s"Set zIndex:$control = $curZ")
    for(idx <- 0 to control.childCount - 1) {
      curZ = depSetZIndex(curZ,control.getChild(idx));
    };
    curZ
  }

  

  def findLastZIndex():Int = {
    this.curDirty match {
      case Some(control) => 
         if(control.getChildIndex > 0) {
           control.getChild(control.getChildIndex - 1).zIndex
         } else if(control.parent.isDefined) {
           control.parent.get.zIndex
         } else {
           0
         }
      case None => 0
    }
  }
}

object UILayer {
    def create(parent:Entity,name:String,index:Int): UILayer = {
      val layerEntity = Entity.New(Some(parent))
      val t = layerEntity.addComponent[Transform]()
      layerEntity.addComponent[Rect2D]()
      layerEntity.addComponent[ContentView]()
      val evNode = layerEntity.addComponent[EventNode]()
      evNode.setThrough(true)

      t.localPosition.z = index * 100
      
      val retLayer = UILayer(name,index)
      retLayer.entity = Some(layerEntity)
      retLayer
    }
}

/*
                 1
         2                 9
    3       6        10        13
  4  5   7   8      11 12   14   15

 
*/