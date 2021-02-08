package seija.data
import seija.math.Vector2

import scala.collection.mutable
import scala.scalajs.js.Error
import scalajs.js
import slogging.LazyLogging

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

  def clear():Unit = {
    this.symbols.clear()
  }
}



object SExprInterp {
  val rootContent:SContent = new SContent
  def init():Unit = {
    this.rootContent.set("pi",SFloat(3.14159f))
    this.rootContent.set("+", SNFunc(InterpCoreFunction.add))
    this.rootContent.set("vec2",SNFunc(InterpCoreFunction.vec2))
    this.rootContent.set("color.red",SUserData(Color.red))
    this.rootContent.set("hex-color",SNFunc(InterpCoreFunction.hexColor))
    this.rootContent.set("color.green",SUserData(Color.green))
    this.rootContent.set("color.blue",SUserData(Color.blue))
    this.rootContent.set("color.transparent",SUserData(Color.transparent))
    this.rootContent.set("color.black",SUserData(Color.black))
    this.rootContent.set("color.white",SUserData(Color.white))

    this.rootContent.set("odd?",SNFunc(InterpCoreFunction.isOdd))
    this.rootContent.set("const",SNFunc(InterpCoreFunction.const))
    this.rootContent.set("if",SNFunc(InterpCoreFunction.ifF))
    this.rootContent.set("do",SNFunc(InterpCoreFunction.doF))
    this.rootContent.set("let",SNFunc(InterpCoreFunction.letF))
    this.rootContent.set("log",SNFunc(InterpCoreFunction.logF))
    this.rootContent.set("str",SNFunc(InterpCoreFunction.strF))
    this.rootContent.set("match",SNFunc(InterpCoreFunction.matchF))
    this.rootContent.set("map",SNFunc(InterpCoreFunction.mapF))

    this.rootContent.set("fs-roots",SNFunc(InterpCoreFunction.fsRoots))
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
          case sFn: SNFunc =>
            sFn.callFn(list.tail,curContent)
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
      case v@SUserData(_) => v
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
      case SUserData(d) => d
    }
  }

  def evalToValue(expr:SExpr,context:Option[SContent]):Any = {
    val evalExpr = eval(expr, context)
    exprToValue(evalExpr)
  }

  def evalStringToValue(string: String,context: Option[SContent] = None):Any = {
    val eExpr = evalString(string,context)
    eExpr.map((expr) => evalToValue(expr,context)).getOrElse(null)
  }
}

private object InterpCoreFunction extends LazyLogging {
  def add(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    var retNumber:Float = 0;
    var allInt:Boolean = true;
    for(numExpr <- evalArgs) {
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

  def vec2(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val x = evalArgs(0).castFloat()
    val y = evalArgs(1).castFloat()
    SUserData(Vector2.New(x, y))
  }

  def hexColor(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val strColor = evalArgs(0).castString()
    Color.colorRead.read(strColor) match {
      case Some(value) => SUserData(value)
      case None => 
        logger.error(strColor +" not color")
        SNil
    }
    
  }

  def isOdd(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    evalArgs(0) match {
      case SInt(value) => SBool(value % 2 == 0)
      case v => throw new Exception(s"$v not int number")
    }
  }

  def const(args:js.Array[SExpr],content: SContent):SExpr =  SExprInterp.eval(args(0),Some(content))

  def ifF(args:js.Array[SExpr],content: SContent):SExpr = {
    val b = SExprInterp.eval(args(0),Some(content)).castBool()
    if(b) {
      SExprInterp.eval(args(1),Some(content))
    } else {
      SExprInterp.eval(args(2),Some(content))
    }
  }

  def doF(args:js.Array[SExpr],content: SContent):SExpr = {
     for(idx <- 0 until args.length) {
       val evalValue = SExprInterp.eval(args(idx),Some(content))
       if(idx == args.length - 1) {
         return evalValue
       }
     }
     SNil
  }

  def letF(args:js.Array[SExpr],content: SContent):SExpr = {
    val newContent = new SContent(Some(content))
    val vecList = args(0).asInstanceOf[SVector].list
    var idx:Int = 0
    while(idx < vecList.length) {
      val symName = vecList(idx).asInstanceOf[SSymbol].value
      val symValue = SExprInterp.eval(vecList(idx + 1),Some(newContent))
      newContent.set(symName,symValue)
      idx += 2
    }
    idx = 1
    while(idx < args.length) {
      val evalValue = SExprInterp.eval(args(idx),Some(newContent))
      idx += 1
      if(idx == args.length) {
        return evalValue
      }
    }
    SNil
  }

  def logF(args:js.Array[SExpr],content: SContent):SExpr = {
    val value = SExprInterp.evalToValue(args(0),Some(content))
    println(value)
    SNil
  }

  def strF(args:js.Array[SExpr],content: SContent):SExpr = {
    if(args.length == 0) {
      return SString("")
    }
    var retString = ""
    for(arg <- args) {
     val s = SExprInterp.eval(arg, Some(content)) match {
        case SBool(value) => value.toString
        case SNil => "nil"
        case SString(value) => value
        case SVector(list) => list.toString
        case SInt(value) => value.toString
        case SFloat(value) => value.toString
        case SKeyword(value) => value
        case SUserData(value) => value.toString
        case v => v.toString
      }
      retString += s
    }
    SString(retString)
  }

  def matchF(args:js.Array[SExpr],content: SContent):SExpr = {
    val matchArg = SExprInterp.eval(args(0),Some(content))
    var idx = 1
    while (idx < args.length) {
      val curExpr = SExprInterp.eval(args(idx),Some(content))
      if(curExpr.eq(matchArg)) {
        return SExprInterp.eval(args(idx + 1),Some(content))
      }
      idx += 2
    }
    SNil
  }

  def mapF(args:js.Array[SExpr],content: SContent):SExpr = {
    val evalArgs = args.map(e => SExprInterp.eval(e,Some(content)))
    val f = evalArgs(0).castFunc();
    val lst = evalArgs(1) match {
      case SList(list) => list
      case SVector(list) => list
      case _ => js.Array()
    };
    val retList:js.Array[SExpr] = js.Array();
    for(item <- lst) {
      val sExpr = f.callByArgs(js.Array(item),Some(content))
      retList.push(sExpr)
    }
    SVector(retList)
  }

  def fsRoots(args:js.Array[SExpr],content: SContent):SExpr = {
    val list:js.Array[SExpr] = seija.os.list.roots().map(_.toString()).map(SString(_));
    SVector(list)
  }
}













