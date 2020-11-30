package demo
import scala.scalajs.js
import seija.data.DynObject

case class TDemoItem(itemId:Int,itemName:String,itemCost:Int)


class TestModel {
   var itemList:js.Array[TDemoItem] = js.Array()

   def init() {
       DynObject.registerClass[TestModel]()
       DynObject.registerClass[TDemoItem]()
       this.itemList.push(TDemoItem(1000,"QQQ",10))
       this.itemList.push(TDemoItem(1200,"DDDD",15))
       this.itemList.push(TDemoItem(1201,"Gold",20))
   }
}