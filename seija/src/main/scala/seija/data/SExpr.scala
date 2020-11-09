package seija.data
import scala.scalajs.js;
import scala.collection;
import scala.collection.immutable.HashSet
import seija.data.ParseString
import scala.util.control._
sealed trait SExpr

case class SSymbol(value:String) extends SExpr
case class SList(list:js.Array[SExpr]) extends SExpr
case class SBool(value:Boolean) extends SExpr
case object SNil extends SExpr
case class SString(value:String) extends SExpr
case class SVector(list:js.Array[SExpr]) extends SExpr
case class SInt(value:Int) extends SExpr
case class SFloat(value:Float) extends SExpr
case class SKeyword(value:String) extends SExpr

class SExprParser(string:String) {
    var parseString:ParseString = new ParseString(string)

    

    def parseSExpr():Either[String,SExpr] = {
        this.parseString.lookNext(1) match {
            case Some('(') => this.parseSList()
            case Some('[') => ???
            case Some(chr) =>
             if(chr.isWhitespace) {
                this.parseString.skipWhile(_.isWhitespace)
                return this.parseSExpr()
             } else if(chr.isDigit) {
                return this.parseSNumber()
             } else if(this.isSymbolStart(chr)) {
                 return this.parseSSymbol()
             }
             ???
            case None => Left("empty string")
        }
    }

    def parseSList():Either[String,SExpr] = {
        this.parseString.moveNext()
        var sExprList:js.Array[SExpr] = js.Array()
        val loop = new Breaks;
        var curSExpr = this.parseSExpr()
        for {
            curSExpr <- this.parseSExpr()
        } yield curSExpr;
        
        loop.breakable {
            while (curSExpr.isRight) {
                sExprList.push(curSExpr.toOption.get)
                curSExpr = this.parseSExpr()

            }
        }
        Left("?AAA")
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

    def parseSSymbol():Either[String,SExpr] = {
        Left("aaa")
    }

    def isSymbol(chr:Char):Boolean = {
        SExprParser.symChars.contains(chr) || chr.isLetterOrDigit
        false
    }
    def isSymbolStart(chr:Char):Boolean = {
        SExprParser.symChars.contains(chr) || chr.isLetter
    }
}

object SExprParser {
    val symChars:HashSet[Char] = HashSet('+','-','*','/','>','<','_','!','-','?','.',':','$','=','&','#')
   

    def parse(string:String):Unit = {
        println("parse: "+string)
        var parser = new SExprParser(string)

        val ret = parser.parseSExpr()
        println(ret)
    }

    
}