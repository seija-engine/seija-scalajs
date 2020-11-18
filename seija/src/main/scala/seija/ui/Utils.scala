package seija.ui
import seija.data.{SExpr, SExprParser}

object Utils {
  def parseParam(string: String):Either[String,SExpr] = {
    if(string.length  == 0) {
      return Left("")
    }
    string.head match {
      case '(' =>
        SExprParser.parse(string) match {
          case Left(value) =>
            println(value)
            Left("error")
          case Right(value) => Right(value)
        }
      case str => Left(string.tail)
    }
  }
}
