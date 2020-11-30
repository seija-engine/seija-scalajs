package seija.data
import scala.scalajs.js
import scala.collection.immutable.HashSet
import scala.collection.mutable
import scala.util.control.Breaks._
import seija.data.SContent
sealed trait SExpr {
    val exprType:Int = 0
    def isFloat:Boolean
    def castFloat():Float = {
        this match {
            case SInt(value) => value
            case SFloat(value) => value
            case _ =>
                println(s"$this cast float error")
                0
        }
    }
    def castKeyword():String = {
        this match {
            case SKeyword(value) => value
            case  _ =>
                println(s"$this cast keyword error")
                ""
        }
    }
    def castSingleAny():Any = {
        this match {
            case SBool(value) => value
            case SNil => null
            case SString(value) => value
            case SInt(value) => value
            case SFloat(value) => value
            case SKeyword(value) => value
            case SUserData(value) => value
            case _ =>
                throw new Exception(s"$this cast castSingleAny error")
                null
        }
    }
    def castBool():Boolean = {
        this match {
            case SBool(value) => value
            case _ =>
                throw new Exception(s"$this cast castBool error")
                false
        }
    }


    def eq(otherExpr: SExpr): Boolean = {
        if(this.exprType != otherExpr.exprType) return false

        this match {
            case SBool(value) => otherExpr.asInstanceOf[SBool].value == value
            case SNil => true
            case SString(value) => otherExpr.asInstanceOf[SString].value == value
            case SVector(list) =>
                val otherList = otherExpr.asInstanceOf[SVector]
                if(list.length != otherList.list.length) return false
                for(idx <- 0 until list.length) {
                    if(!list(idx).equals(otherList.list(idx))) {
                        return false
                    }
                }
                true
            case SInt(value) => otherExpr.asInstanceOf[SInt].value == value
            case SFloat(value) => otherExpr.asInstanceOf[SFloat].value == value
            case SKeyword(value) => otherExpr.asInstanceOf[SKeyword].value == value
            case SUserData(value) => otherExpr.asInstanceOf[SUserData].value == value
            case _ => false
        }
    }
}

case class SSymbol(value:String) extends SExpr {
    override val exprType: Int = 1
    def isFloat = false
}
case class SList(list:js.Array[SExpr]) extends SExpr {
    override val exprType: Int = 2
    def isFloat = false
}
case class SBool(value:Boolean) extends SExpr {
    override val exprType: Int = 3
    def isFloat = false
}
case object SNil extends SExpr {
    override val exprType: Int = 4
    def isFloat = false
}
case class SString(value:String) extends SExpr {
    override val exprType: Int = 5
    def isFloat = false
}
case class SVector(list:js.Array[SExpr]) extends SExpr {
    override val exprType: Int = 6
    def isFloat = false
}
case class SInt(value:Int) extends SExpr {
    override val exprType: Int = 7
    def isFloat = false
}
case class SFloat(value:Float) extends SExpr {
    override val exprType: Int = 8
    def isFloat = true
}
case class SKeyword(value:String) extends SExpr {
    override val exprType: Int = 9
    def isFloat = false
}
case class SFunc(args:js.Dictionary[SExpr],list:js.Array[SExpr]) extends SExpr {
    override val exprType: Int = 10
    override def toString: String = {
        val argsString = if(args.isEmpty) "" else args.keys.reduce((a,b) => a + b)
        s"SFunc([$argsString],${list.toString})"
    }
    def isFloat = false
    def call(content: Option[SContent]):SExpr = {
        SExprInterp.eval(SList(list),content)
    }
    def callByArgs(args:js.Array[SExpr],content: Option[SContent]):SExpr = {
        val callContext = new SContent(content)
        var idx = 1
        for(arg <- args) {
            val key =  if(idx == 1) "%" else "%" + idx.toString
            callContext.set(key,arg)
        }
        SExprInterp.eval(SList(list),Some(callContext))
    }
}
case class SObject(value:mutable.HashMap[SExpr,SExpr]) extends SExpr {
    override val exprType: Int = 11
    def isFloat = false
}

case class SNFunc(val callFn:(js.Array[SExpr],SContent) => SExpr) extends SExpr {
    override val exprType: Int = 12
    def isFloat = false
}
case class SUserData(val value:Any) extends SExpr {
    override val exprType: Int = 13
    def isFloat = false
}

object SExpr {
    def fromAny(value:Any) :SExpr = {
        value match {
            case v: String => SString(v)
            case v: Boolean => SBool(v)
            case v:Int => SInt(v)
            case v:Float => SFloat(v)
            case v => SUserData(v)
        }
    }
}

class SExprParser(string:String) {
    var parseString:ParseString = new ParseString(string)
    var putArgs:js.Array[js.Array[String]] = js.Array()
    def parseSExpr():Either[String,SExpr] = {
        this.parseString.lookNext(1) match {
            case Some('(') =>
                this.parseString.moveNext()
                this.parseList(')').map(arr => SList(arr))
            case Some('[') =>
                this.parseString.moveNext()
                this.parseList(']').map(arr => SVector(arr))
            case Some('{') =>
                this.parseString.moveNext()
                this.parseObject()
            case Some(':') =>
                val symString = this.parseString.takeWhile(isSymbol)
                Right(SKeyword(symString))
            case Some('"') =>
                this.takeString('"').map(str => SString(str))
            case Some('\'') =>
                this.takeString('\'').map(str => SString(str))
            case Some('#') =>
                this.parseLambda()
            case Some('%') =>
                this.parseString.moveNext()
                val lastArray = this.putArgs.last
                if(this.parseString.lookNext(1).exists(_.isDigit)) {
                    val argString = "%" + this.parseString.takeWhile(_.isDigit)
                    lastArray.push(argString)
                    Right(SSymbol(argString))
                }else {
                    lastArray.push("%")
                    Right(SSymbol("%"))
                }
            case Some(chr) if chr.isWhitespace =>
                this.parseString.skipWhile(_.isWhitespace)
                this.parseSExpr()
            case Some(chr) if chr.isDigit =>
                this.parseSNumber()
            case Some(chr) if isSymbolStart(chr) =>
                val symString = this.parseString.takeWhile(this.isSymbol)
                symString match {
                    case "nil" => Right(SNil)
                    case "true" => Right(SBool(true))
                    case "false" => Right(SBool(false))
                    case str => Right(SSymbol(str))
                }
            case Some(chr) =>
                Left("error char " + chr)
            case None => Left("empty string")
        }
    }

    def parseLambda():Either[String,SExpr] = {
        this.putArgs.push(js.Array())
        this.parseString.moveNext()
        this.parseSExpr() match {
            case Left(value) => Left(value)
            case Right(SList(list)) =>
                val lastArray = this.putArgs.pop()
                val argDic:js.Dictionary[SExpr] = js.Dictionary()
                for(s <- lastArray) {
                    argDic.put(s,SNil)
                }
                Right(SFunc(argDic,list))
            case Right(_) => Left("error func type")
        }
    }

    def parseObject():Either[String,SExpr] = {
        var dic:mutable.HashMap[SExpr,SExpr] = mutable.HashMap()
        breakable {
            while(true) {
                this.parseString.skipWhile(_.isWhitespace)
                if(this.parseString.lookNext(1).contains('}')) {
                    this.parseString.moveNext()
                    break
                }
                this.parseString.skipWhile(_.isWhitespace)
                val keyExpr = this.parseSExpr()
                if(keyExpr.isLeft) {
                    return Left("object key error:"+keyExpr.toString)
                }
                this.parseString.skipWhile(_.isWhitespace)
                val valueExpr = this.parseSExpr()
                if(valueExpr.isLeft) {
                    return Left("object value error:"+valueExpr.toString)
                }
                val key:SExpr = keyExpr.toOption.get
                dic.put(key,valueExpr.toOption.get)
            }
        }

        Right(SObject(dic))
    }

    def parseList(endChr:Char):Either[String,js.Array[SExpr]] = {
        val sExprList:js.Array[SExpr] = js.Array()
        breakable {
            while (true) {
                this.parseString.skipWhile(_.isWhitespace)
                if(this.parseString.lookNext(1).contains(endChr)) {
                    this.parseString.moveNext()
                    return Right(sExprList)
                }
                val curSExpr = this.parseSExpr()
                if(curSExpr.isLeft) {
                    return Left(curSExpr.left.getOrElse(""))
                }
                sExprList.push(curSExpr.toOption.get)
                this.parseString.skipWhile(c => c.isWhitespace || c == ',')
            }
        }
        Right(sExprList)
    }



    def parseSNumber():Either[String,SExpr] = {
        val intString = this.parseString.takeWhile(_.isDigit)
        if(this.parseString.lookNext(1).contains('.')) {
            this.parseString.moveNext()
            val floatString = this.parseString.takeWhile(_.isDigit)
            Right(SFloat((intString+'.'+floatString).toFloat))
        } else {
            Right(SInt(intString.toInt))
        }
    }

    def takeString(strChr:Char):Either[String,String] = {
        this.parseString.moveNext()
        val string = this.parseString.takeWhile(chr => chr != strChr)
        if(this.parseString.lookNext(1).contains(strChr)) {
            this.parseString.moveNext()
            Right(string)
        } else {
            Left("parse string error")
        }
    }

    def isSymbol(chr:Char):Boolean = SExprParser.symChars.contains(chr) || chr.isLetterOrDigit

    def isSymbolStart(chr:Char):Boolean = SExprParser.symChars.contains(chr) || chr.isLetter

}

object SExprParser {
    val symChars:HashSet[Char] = HashSet('+','-','*','/','>','<','_','!','-','?','.',':','$','=','&','#')
   

    def parse(string:String):Either[String,SExpr] = {
        val parser = new SExprParser(string)
        parser.parseSExpr()
    }

    
}