package seija.ui

import seija.data.SContent
import seija.data.SExpr
import seija.data.SExprInterp
import seija.data.SExprParser
import slogging.LazyLogging
import seija.data.SNil

object Utils extends LazyLogging {
   def parseParam(str:String,content:SContent):Either[String,SExpr] = {
       if(str.startsWith("@")) {
           return Left(str.substring(1))
       }
       if(str.startsWith("(") || str.startsWith("[") || str.startsWith("#(") || str.startsWith("{")) {
           SExprParser.parse(str) match {
               case Left(value) => 
                 logger.error(value) 
                 return Right(SNil)
               case Right(expr) => return Right(expr)
           }
       }
       return Left(str)
   }
}