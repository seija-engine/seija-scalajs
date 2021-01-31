package seija.os

object PathError {
  type IAE = IllegalArgumentException
  private[this] def errorMsg(s: String, msg: String) =
    s"[$s] is not a valid path segment .$msg"

  case class InvalidSegment(segment: String, msg: String)
      extends IAE(errorMsg(segment, msg))

  case object AbsolutePathOutsideRoot
      extends IAE(
        "The path created has enough ..s that it would start outside the root directory"
      )

  case class NoRelativePath(src: RelPath, base: RelPath)
      extends IAE(s"Can't relativize relative paths $src from $base")
}

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

object BasePath {

  def checkSegment(s: String) = {
    def fail(msg: String) = throw PathError.InvalidSegment(s, msg)
    def considerStr =
      "use the Path(...) or RelPath(...) constructor calls to convert them. "
    s.indexOf('/') match {
      case -1 => // do noting
      case c =>
        fail(
          s"[/] is not a valid character to appear in a path segment. " +
            "If you want to parse an absolute or relative path that may have " +
            "multiple segments, e.g. path-strings coming from external sources " +
            considerStr
        )
    }
    def externalStr =
      "If you are dealing with path-strings coming from external sources, "
    s match {
      case "" =>
        fail(
          "OS-Lib does not allow empty path segments " +
            externalStr + considerStr
        )
      case "." =>
        fail(
          "OS-Lib does not allow [.] as a path segment " +
            externalStr + considerStr
        )
      case ".." =>
        fail(
          "OS-Lib does not allow [..] as a path segment " +
            externalStr +
            considerStr +
            "If you want to use the `..` segment manually to represent going up " +
            "one level in the path, use the `up` segment from `os.up` " +
            "e.g. an external path foo/bar/../baz translates into 'foo/'bar/up/'baz."
        )
      case _ =>
    }
  }

  def chunkify(s:RawPath) {
    
  }


}

sealed trait FilePath extends BasePath {
  def resolveFrom(base: Path): Path
}

class RelPath private[os] (segments0: Array[String], val ups: Int) {}

class Path(val rawPath: RawPath) {
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
