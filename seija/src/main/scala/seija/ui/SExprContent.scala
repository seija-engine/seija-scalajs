package seija.ui
import scala.scalajs.js
import seija.data._
import seija.s2d.layout.LRate
import seija.s2d.layout.LConst

object  SExprContent {
   val content:SContent = new SContent(Some(SExprInterp.rootContent))

   def init() {
       content.set("env",SNFunc(env))
       content.set("attr-bind",SNFunc(attrBind))
       content.set("attr-fbind",SNFunc(attrFBind))
       content.set("attr",SNFunc(attr))
       content.set("num-rate",SNFunc(numRate))
       content.set("num-const",SNFunc(numConst))
       content.set("emit",SNFunc(emit))
   }

   def env(args:js.Array[SExpr],content:SContent):SExpr = {
      val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
      val envName = args(0).asInstanceOf[SSymbol].value
      val findValue = UISystem.findEnv(envName)
      SUserData(findValue)
   }

   def emit(args:js.Array[SExpr],content:SContent):SExpr = {
      val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
      val evName = evalArgs(0).castKeyword()
      val ownerControlExpr = content.find("ownerControl");
      if(ownerControlExpr.isEmpty) return SNil
      val control = ownerControlExpr.get.castSingleAny().asInstanceOf[Control]
      control.handleEvent(evName,evalArgs.tail)
      
      SNil
   }

   def attr(args:js.Array[SExpr],content:SContent):SExpr = {
      val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
      val attrName = evalArgs(0).castKeyword().substring(1)
      val ownerControlExpr = content.find("ownerControl");
      if(ownerControlExpr.isDefined) {
         val control = ownerControlExpr.get.castSingleAny().asInstanceOf[Control]
         val getValue = control.getProperty(attrName)
          if(getValue.isDefined) {
             return SUserData(getValue.get) 
          }
      }
      SNil
   }

   def attrBind(args:js.Array[SExpr],content:SContent):SExpr = {
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

   def attrFBind(args:js.Array[SExpr],content:SContent):SExpr = {
      val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
      val attrName = evalArgs(0).castKeyword().substring(1)
      val ownerControlExpr = content.find("ownerControl");
      val setFuncExpr = content.find("setFunc")
      if(setFuncExpr.isEmpty || ownerControlExpr.isEmpty) return SNil
      val control = ownerControlExpr.get.castSingleAny().asInstanceOf[Control]
      val setFunc = setFuncExpr.get.castSingleAny().asInstanceOf[(Any) => Unit]
      val exprFunc = evalArgs(1).asInstanceOf[SFunc]

      val callFunc = (attrValue:Any) => {
        
        val arg = SExpr.fromAny(attrValue)
        val evalValue = exprFunc.callByArgs(js.Array(arg),Some(content))
        setFunc(evalValue.castSingleAny())
      };
      val oldValue = control.getProperty(attrName)
      if(oldValue.isDefined) {
         callFunc(oldValue.get)
      }
      control.addPropertyLister[Any](attrName,callFunc)
      SNil
   }

   def numRate(args:js.Array[SExpr],content:SContent):SExpr = {
      val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
      SUserData(LRate(evalArgs(0).castFloat()))
   }

   def numConst(args:js.Array[SExpr],content:SContent):SExpr = {
      val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
      SUserData(LConst(evalArgs(0).castFloat()))
   }
}