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
      val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
      val attrName = evalArgs(0).castKeyword().substring(1)
      val ownerControlExpr = content.find("ownerControl");
      val setFuncExpr = content.find("setFunc")
      if(setFuncExpr.isEmpty) return SNil
      if(ownerControlExpr.isDefined) {
        val control = ownerControlExpr.get.castSingleAny().asInstanceOf[Control]
        val setFunc = setFuncExpr.get.castSingleAny().asInstanceOf[(Any) => Unit]
        val getValue = control.getProperty(attrName)
        if(getValue.isDefined) {
           setFunc(getValue.get)
        }
        control.addPropertyLister[Any](attrName,setFunc)
      }
      SNil
   }
}