package seija.ui
import scala.scalajs.js
import seija.data._

object  SExprContent {
   val content:SContent = new SContent(Some(SExprInterp.rootContent))

   def init() {
       content.set("env",SNFunc(env))
       content.set("attr",SNFunc(attr))
   }

   def env(args:js.Array[SExpr],content:SContent):SExpr = {
    val envName = args(0).asInstanceOf[SSymbol].value
    val findValue = UISystem.findEnv(envName)
    SUserData(findValue)
   }

   def attr(args:js.Array[SExpr],content:SContent):SExpr = {
      val ownerControl = content.find("ownerControl");
      println(ownerControl.isDefined)
      SNil
   }
}