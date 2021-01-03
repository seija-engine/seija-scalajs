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
   protected var entity:Option[Entity] = None
   def getEntity:Entity = entity.get
   val sContext:SContent = new SContent(Some(SExprContent.content))
   val slots:js.Dictionary[Control] = js.Dictionary()
   
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

   def init(parent:Option[Control],params:ControlParams) {
      this.parent = parent
      this.sContext.set("control",SUserData(this))
   }

   def createChild(params:ControlParams) {
      val childXml = params.paramXmls.get("Children")
      if(childXml.isEmpty || !this.slots.contains("Children")) return
     
      for(childNode <- childXml.get.children.getOrElse(js.Array())) { 
         UISystem.createByXml(childNode,Some(this.slots("Children")),ControlParams(),params.nsPaths) match {
            case Left(errString) => logger.error(errString)
            case Right(control) => this.children.push(control)
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