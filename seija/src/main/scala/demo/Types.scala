package demo
import scala.scalajs.js
import seija.data.DynObject
import seija.ui2.IBehavior
import seija.data.SExpr
import seija.data.SVector
import seija.data.SExprInterp
import seija.data.SKeyword
import seija.data.SInt

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
           val deleteId:Int = SExprInterp.exprToValue(evData(0)).asInstanceOf[Int]
           val deleteIndex = this.itemList.indexWhere(_.itemId == deleteId)
           if(deleteIndex >= 0) {
               this.itemList.remove(deleteIndex)
               this.emit(":UpdateItem",js.Array(SKeyword(":Delete"),SInt(deleteIndex)))
           }
          case _ => ()
      }
   }
}