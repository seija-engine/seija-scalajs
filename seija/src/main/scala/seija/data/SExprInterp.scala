package seija.data
import scala.collection.mutable
import scalajs.js

class SContent(var parent:Option[SContent] = None) {
  val symbols:js.Dictionary[SExpr] = js.Dictionary()
  def set(name:String,value:SExpr):Unit = {
    this.symbols.put(name,value)
  }

  def find(name:String):Option[SExpr] = {
    val v = this.symbols.get(name)
    if(v.isDefined) return v
    if(parent.isDefined) return parent.get.find(name)
    None
  }
}



object SExprInterp {
  val rootContent:SContent = new SContent
  def init():Unit = {
    this.rootContent.set("pi",SFloat(3.14159f))
    this.rootContent.set("+", SNFunc(InterpCoreFunction.add))
  }

  def eval(expr:SExpr,context: Option[SContent] = None):SExpr = {
    val curContent = context.getOrElse(rootContent)
    expr match {
      case SSymbol(value) =>
        curContent.find(value).getOrElse(SNil)
      case lst@SList(list) =>
        if(list.length == 0) {
          return lst
        }
        val headSym = eval(list.head,context)
        headSym match {
          case SNil =>
            println("not found "+ list.head + " in content")
            SNil
          case sFn: SNFunc => sFn.callFn(list.tail,curContent)
          case _ =>
            println("list head must is function")
            SNil
        }
      case v@SBool(_) => v
      case v@SNil => v
      case v@SString(_) => v
      case SVector(list) =>
        SVector(list.map(v => eval(v,context)))
      case v@SInt(_) => v
      case v@SFloat(_) => v
      case v@SKeyword(_) => v
      case expr@SFunc(_, _) => expr
      case SObject(obj) =>
        val retMap:mutable.HashMap[SExpr,SExpr] = mutable.HashMap()
        for((keyExpr,valueExpr) <- obj) {
          val k = eval(keyExpr,context)
          val v = eval(valueExpr,context)
          retMap.put(k,v)
        }
        SObject(retMap)
      case v@SNFunc(_) => v
    }
  }

  def evalString(string: String,context: Option[SContent] = None ):Either[String,SExpr] = {
    SExprParser.parse(string).map(expr => SExprInterp.eval(expr,context))
  }

  def exprToValue(expr:SExpr):Any = {
    expr match {
      case SSymbol(value) => null
      case SList(list) => list.map(v => exprToValue(v))
      case SBool(value) => value
      case SNil => null
      case SString(value) => value
      case SVector(list) => list.map(v => exprToValue(v))
      case SInt(value) => value
      case SFloat(value) => value
      case SKeyword(value) => value
      case v@SFunc(args, list) => v
      case SObject(value) =>
        val retMap:mutable.HashMap[Any,Any] = mutable.HashMap()
        for((k,v) <- value) {
          retMap.put(exprToValue(k),exprToValue(v))
        }
        retMap
      case v@SNFunc(callFn) => v
    }
  }
}

private object InterpCoreFunction {
  def add(args:js.Array[SExpr],content: SContent):SExpr = {
    var retNumber:Float = 0;
    var allInt:Boolean = true;
    for(numExpr <- args) {
      numExpr match {
        case SInt(value) =>
          retNumber = retNumber + value
        case SFloat(value) =>
          retNumber = retNumber + value
          allInt = false
        case expr =>
          println(expr.toString + " not number")
          return SNil
      }
    }
    if(allInt) SInt(retNumber.asInstanceOf[Int]) else SFloat(retNumber)
  }
}