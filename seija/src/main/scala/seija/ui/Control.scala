package seija.ui
import scala.scalajs.js
import seija.core.Entity
import seija.data._
import seija.ui.SExprContent
import seija.data.SUserData
import slogging.LazyLogging



case class ControlParams(paramStrings:js.Dictionary[String] = js.Dictionary(),
                         paramXmls:js.Dictionary[XmlNode] = js.Dictionary(),
                         var children:js.Array[XmlNode] = js.Array(),
                         var nsPaths:js.Dictionary[String] = js.Dictionary())


trait ControlCreator[+T <: Control] {
   val name:String
   def init()
   def create():T
}

class Control extends LazyLogging {
   var parent:Option[Control] = None
   var children:js.Array[Control] = js.Array()
   var entity:Option[Entity] = None
   def getEntity:Entity = entity.get
   val sContext:SContent = new SContent(Some(SExprContent.content))
   val slots:js.Dictionary[Control] = js.Dictionary()
   var mainTemplate:Option[UITemplate] = None
   var ownerControl:Option[Control] = None
   
   protected var property: js.Dictionary[Any] = js.Dictionary()
   protected var propertyListers:js.Dictionary[js.Array[IndexedRef]] = js.Dictionary()

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

   def init(parent:Option[Control],params:ControlParams,ownerControl:Option[Control] = None) {
      this.parent = parent
      this.ownerControl = ownerControl
      this.sContext.set("control",SUserData(this))
      ownerControl.foreach(oc => this.sContext.set("ownerControl",SUserData(oc)))
      this.mainTemplate = params.paramXmls.get("Template").map(xmlNode => new UITemplate(xmlNode,this));

      this.initProperty[String]("OnEnter",params.paramStrings,None)
   }

   def createChild(params: ControlParams):Unit = {
      if(!params.paramXmls.contains("Children")) return

      val childArray = params.paramXmls("Children")
      for(child <- childArray.children.getOrElse(js.Array())) {
         if(child.tag.startsWith("Slot.")) {
            if(ownerControl.isDefined) {
               ownerControl.get.slots.put(child.tag.substring("Slot.".length()),this.ownerControl.get)
            }
         } else {
            UISystem.createByXml(child,this.slots.get("Children"),ControlParams(),this.ownerControl) match {
               case Left(value) => logger.error(value)
               case Right(value) => ()
            }
         }
      }
   }

   def initProperty[T](key:String,params:js.Dictionary[String],defValue:Option[T])(implicit read:Read[T]) {
      params.get(key) match {
         case Some(strValue) =>
            Utils.parseParam(strValue,this.sContext) match {
               case Left(strValue) => 
                 val readValue = read.read(strValue)
                  readValue.foreach(setProperty(key,_))
                  if(readValue.isEmpty) {
                     logger.error(s"read property error $key = $strValue")
                  }
               case Right(sExpr) =>
                 SExprInterp.eval(sExpr,Some(sContext)) match {
                    case SNil => ()
                    case evalExpr => setProperty(key,evalExpr.toValue[Any])
                 }
            }
         case None => defValue.foreach(setProperty(key,_))
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
   
   def OnEnter() {}

   def destroy() {}

   def OnDestroy() {}
}