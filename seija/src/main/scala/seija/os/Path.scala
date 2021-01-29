package seija.os

trait PathChunk {
  def segments: Seq[String]
  def ups: Int
}

trait BasePath {
   type ThisType <: BasePath

   def /(chunk: PathChunk): ThisType

   def relativeTo(target: ThisType): RelPath

   def startsWith(target: ThisType): Boolean
 
   def endsWith(target: RelPath): Boolean
}

sealed trait FilePath extends BasePath {
  def resolveFrom(base: Path): Path
}



class RelPath private[os](segments0: Array[String], val ups: Int) {
   
}

class Path(val rawPath:RawPath) {
   override def toString(): String = rawPath.toString   
}


sealed trait PathConvertible[T] {
  def apply(t: T): RawPath
}

object PathConvertible {
  implicit object StringConvertible extends PathConvertible[String] {
    def apply(path: String): RawPath = RawPath.fromString(path)
  }
}