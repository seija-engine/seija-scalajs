package demo
import scala.scalajs.js
import seija.data.{DynObject, SExpr, SExprInterp, SInt, SKeyword, SUserData, SVector}
import seija.ui2.IBehavior

case class TDemoItem(itemId:Int,itemName:String,itemCost:Int)


class TestModel extends IBehavior {
   var itemList:js.Array[TDemoItem] = js.Array()

   def init() {
       DynObject.registerClass[TestModel]()
       DynObject.registerClass[TDemoItem]()
       this.itemList.push(TDemoItem(1200,"DDDD",15))
       this.itemList.push(TDemoItem(1201,"Gold",20))
   }

   override def handleEvent(evKey:String,evData: js.Array[SExpr]): Unit = {
      evKey match {
          case ":DeleteItem" =>
           val deleteId:Int = evData(0).toValue[Int]
           val deleteIndex = this.itemList.indexWhere(_.itemId == deleteId)
           if(deleteIndex >= 0) {
               this.itemList.remove(deleteIndex)
               this.emit(":UpdateItem",js.Array(SKeyword(":Delete"),SInt(deleteIndex)))
           }
          case ":AddItem" =>
            val itemName = evData(0).toValue[String]
            val maxId = this.itemList.foldLeft(0)((id,b) => Math.max(id,b.itemId))
            val demoItem = TDemoItem(maxId + 1,itemName,200)
            this.itemList.push(demoItem)
            this.emit(":UpdateItem",js.Array(SKeyword(":Add"),SUserData(demoItem)))
          case ":CheckItem" =>
            val itemId:Int = evData(0).toValue[Int]
            val itemIndex = this.itemList.indexWhere(_.itemId == itemId)
            this.emit(":UpdateItem",js.Array(SKeyword(":Check"),SInt(itemIndex)))
          case _ => ()
      }
   }
}