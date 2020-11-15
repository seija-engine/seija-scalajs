package seija.data
import scala.scalajs.js
import scala.collection.immutable.HashSet
import scala.collection.mutable
import scala.util.control.Breaks._
import seija.data.SContent
sealed trait SExpr {
    def isFloat:Boolean
}

case class SSymbol(value:String) extends SExpr {
    def isFloat = false
}
case class SList(list:js.Array[SExpr]) extends SExpr {
    def isFloat = false
}
case class SBool(value:Boolean) extends SExpr {
    def isFloat = false
}
case object SNil extends SExpr {
    def isFloat = false
}
case class SString(value:String) extends SExpr {
    def isFloat = false
}
case class SVector(list:js.Array[SExpr]) extends SExpr {
    def isFloat = false
}
case class SInt(value:Int) extends SExpr {
    def isFloat = false
}
case class SFloat(value:Float) extends SExpr {
    def isFloat = true
}
case class SKeyword(value:String) extends SExpr {
    def isFloat = false
}
case class SFunc(args:js.Dictionary[SExpr],list:js.Array[SExpr]) extends SExpr {
    override def toString: String = {
        val argsString = if(args.isEmpty) "" else args.keys.reduce((a,b) => a + b)
        s"SFunc([$argsString],${list.toString})"
    }
    def isFloat = false
}
case class SObject(value:mutable.HashMap[SExpr,SExpr]) extends SExpr {
    def isFloat = false
}

case class SNFunc(val callFn:(js.Array[SExpr],SContent) => SExpr) extends SExpr {
    def isFloat = false
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