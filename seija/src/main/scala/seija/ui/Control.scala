package seija.ui
import scala.scalajs.js
import seija.core.Entity
import seija.data._
import seija.ui.SExprContent
import seija.data.SUserData
import slogging.LazyLogging
import seija.s2d.layout.LayoutView
import seija.math.Vector3
import seija.core.Transform
import seija.math.Vector2
import seija.s2d.Rect2D
import seija.core.Screen


case class ControlParams(paramStrings:js.Dictionary[String] = js.Dictionary(),
                         paramXmls:js.Dictionary[XmlNode] = js.Dictionary(),
                         var children:js.Array[XmlNode] = js.Array(),
                         var nsPaths:js.Dictionary[String] = js.Dictionary(),
                         paramAny:js.Dictionary[Any] = js.Dictionary())


trait ControlCreator[+T <: Control] {
   val name:String
   def init()
   def create():T
}

class Control extends LazyLogging {
   var parent:Option[Control] = None
   protected var children:js.Array[Control] = js.Array()
   def getChild(idx:Int):Control = this.children(idx)
   def childCount:Int = this.children.length
   private var childIndex:Int = -1
   def getChildIndex:Int = childIndex
   var entity:Option[Entity] = None
   def getEntity:Entity = entity.get
   protected var _view:Option[LayoutView] = None
   def getView:Option[LayoutView] = _view
   val sContext:SContent = new SContent(Some(SExprContent.content))
   val slots:js.Dictionary[Control] = js.Dictionary()
   var mainTemplate:Option[UITemplate] = None
   var ownerControl:Option[Control] = None
   protected var property: js.Dictionary[Any] = js.Dictionary()
   protected var propertyListers:js.Dictionary[js.Array[IndexedRef]] = js.Dictionary()
   def layerName:String = this.getProperty("layer").getOrElse("Default")
   var _zIndex:Int = -1
   def zIndex = _zIndex
   def zIndex_=(newVal:Int) = {
      _zIndex = newVal
      val t = this.entity.get.getComponent[Transform]();
      this.parent match {
         case Some(parent) =>
            t.get.localPosition.z = (_zIndex - parent.zIndex) * -0.01f; 
         case None => 
            t.get.localPosition.z = _zIndex * -0.01f;
      }
      //logger.info(s"set Z ${_zIndex}: ${this} = ${t.get.localPosition.z}")
   }
   
   def setProperty(key:String,value:Any) {
      if(this.property.contains(key)) {
         this.property.update(key,value)
      } else {
         this.property.put(key,value)
      }
      var listers = this.propertyListers.get(key)
      if(listers.isDefined) {
         listers.get.foreach(idxRef => {
            idxRef.value.asInstanceOf[Any => ()](value)
         })
      }
   }

   def getProperty[T](key:String):Option[T] = {
      this.property.get(key).map(_.asInstanceOf[T])
   }

   
   def init(parent:Option[Control],
            params:ControlParams,
            ownerControl:Option[Control] = None) {
      if(parent.isDefined) {
         this.setProperty("layer",parent.get.layerName);
         parent.get.addChild(this);
      }
      this.ownerControl = ownerControl
      this.sContext.set("control",SUserData(this))
      ownerControl.foreach(oc => this.sContext.set("ownerControl",SUserData(oc)))
      this.mainTemplate = params.paramXmls.get("Template").map(xmlNode => new UITemplate(xmlNode,this));
      this.initProperty[String]("OnEnter",params.paramStrings,None,None)
      if(this.property.get("layer").isEmpty) {
         this.initProperty[String]("layer",params.paramStrings,Some("Default"),None)
      }
      val entity = Entity.New(parent.flatMap(_.entity))
      this.entity = Some(entity)
      
      this.OnInit(parent,params,ownerControl)
      this.mainTemplate.foreach(_.create())
      this.createChild(params)

      if(parent.isEmpty) {
         UISystem.getLayer(this.layerName).foreach(_.addControl(this))
      }

      this.OnEnter()
   }

   def getChildSort():js.Array[Int] = {
      var array:js.Array[Int] = js.Array();
      var curControl:Option[Control] = Some(this);
      while(curControl.isDefined) {
         array.push(curControl.get.childIndex)
         curControl = curControl.get.parent
      }
      array.reverseInPlace()
   }

   

   def addChild(child:Control) {
      child.parent = Some(this)
      this.children.push(child)
      child.entity.foreach(_.setParent(this.entity))
      for(idx <- 0 to this.children.length - 1) {
         this.children(idx).childIndex = idx
      }
      UISystem.getLayer(this.layerName).foreach(_.DiffUpdateDirty(child))
   }

   def removeChild(child:Control,isDestroy:Boolean = true) {
      UISystem.getLayer(this.layerName).foreach(_.OnRemoveControl(child))
      val index = this.children.indexOf(child);
      if(index > 0) {
         this.children.remove(index)
      }
     
      for(idx <- 0 to this.children.length - 1) {
         this.children(idx).childIndex = idx
      }
      if(isDestroy) {
         child._destroy()
      } else {
          child.entity.foreach(_.setParent(None))
      }
   }

  

   def OnInit(parent:Option[Control],params:ControlParams,ownerControl:Option[Control] = None) {}

   /*
   def setParent(parent:Option[Control]) {
      if(this.parent == parent) return
      if(this.parent.isDefined) {
         parent.get.removeChild(this)
      }
      this.parent = parent
      if(this.parent.isDefined) {
         this.parent.get.addChild(this)
      } else {
         offsetByLayer()
      }
   }*/

   def minPos():Option[Vector2] = {
      if(this.entity.isEmpty) return None;
      val t = this.entity.get.getComponent[Transform]();
      val rect = this.entity.get.getComponent[Rect2D]();
      if(t.isEmpty || rect.isEmpty) return None;
      val pos =  t.get.globalPosition;
      val rectSize = rect.get.size;
      val rectAnchor = rect.get.anchor;
      val xoffset = -rectSize.x * rectAnchor.x;
      val yoffset = rectSize.y * (1 - rectAnchor.y);
      val x:Float = if(pos.x < 0) {
         (Screen.width * 0.5f) - math.abs(pos.x)
      } else {
         (Screen.width * 0.5f) + math.abs(pos.x)
      }
      
      val y:Float = if(pos.y < 0) {
         (Screen.height * 0.5f) + math.abs(pos.y)
      } else {
         (Screen.height * 0.5f) - pos.y
      }
      Some(Vector2.New(x + xoffset,y + yoffset))
   }

   def createChild(params: ControlParams):Unit = {
      if(!params.paramXmls.contains("Children") && params.children.length == 0) return
      val childArray:js.Array[XmlNode] = if(params.children.length > 0) { 
         params.children 
      } else { 
         params.paramXmls.get("Children").flatMap(_.children.toOption).getOrElse(js.Array())
      }
      for(child <- childArray) {
         if(child.tag.startsWith("Slot.")) {
            if(ownerControl.isDefined) {
               ownerControl.get.slots.put(child.tag.substring("Slot.".length()),this)
            }
         } else {
            UISystem.createByXml(child,this.slots.get("Children"),ControlParams(),this.ownerControl) match {
               case Left(value) => logger.error(value)
               case Right(value) => ()
            }
         }
      }
   }

   def initProperty[T](key:String,params:js.Dictionary[String],defValue:Option[T],callFn:Option[T => ()])(implicit read:Read[T]) {
      params.get(key) match {
         case Some(strValue) =>
            Utils.parseParam(strValue,this.sContext) match {
               case Left(strValue) => 
                 val readValue = read.read(strValue)
                  readValue.foreach(setProperty(key,_))
                  if(readValue.isEmpty) {
                     logger.error(s"read property error $key = $strValue")
                  }
                  if(callFn.isDefined) {
                     callFn.get(readValue.get)
                     this.addPropertyLister(key,callFn.get)
                  }
               case Right(sExpr) =>
                 val callContext = callFn match {
                    case Some(value) => 
                      this.addPropertyLister(key,value)
                      val newContext = new SContent(Some(this.sContext))
                      newContext.set("setFunc",SUserData(value))
                      Some(newContext)
                    case None => Some(this.sContext)
                 }
                 SExprInterp.eval(sExpr,callContext) match {
                    case SNil => ()
                    case evalExpr => setProperty(key,evalExpr.toValue[Any])
                 }
            }
         case None => 
         if(callFn.isDefined) {
            this.addPropertyLister(key,callFn.get)
         }
         defValue.foreach(setProperty(key,_))
      }
   }

   def addPropertyLister[T](key:String,callFn:T => ()):IndexedRef = {
      if(!this.propertyListers.contains(key)) {
         this.propertyListers.put(key,js.Array())
      }
      val arr:js.Array[IndexedRef] = this.propertyListers(key)
      val refValue:IndexedRef = IndexedRef(arr.length,callFn)
      arr.push(refValue)
      refValue
   }

   def removePropertyLister(key:String,idxRef:IndexedRef) {
      if(!this.propertyListers.contains(key)) return
      val arr = this.propertyListers(key)
      arr.remove(idxRef.index)
      for(idx <- 0 until arr.length) {
         arr(idx).index = idx
      }
   }

   def handleEvent(evKey: String, evData: js.Array[SExpr]): Unit = {
      this.parent.foreach(_.handleEvent(evKey,evData))
   }
   
   def OnEnter() {
      //logger.info(this.getChildSort().toString())
   }

   def destroy() {
      this.parent match {
         case Some(parent) =>
            parent.removeChild(this,false)
         case None =>
            UISystem.getLayer(this.layerName).foreach(_.removeControl(this))
      }
      this._destroy()
   }

   protected def _destroy() {
      this.children.foreach(_._destroy())
      this.entity.foreach(_.destroy())
      this.children.clear();
      this.OnDestroy()
   }

   def OnDestroy() {}
}