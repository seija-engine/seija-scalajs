package seija.os
import scala.scalajs.js;

trait PathChunk {
  def segments: Seq[String]
  def ups: Int
}

object PathChunk {
  implicit class RelPathChunk(r: RelPath) extends PathChunk {
    def segments = r.segments
    def ups = r.ups
    override def toString() = r.toString
  }

  implicit class StringPathChunk(s: String) extends PathChunk {
    BasePath.checkSegment(s)
    def segments = Seq(s)
    def ups = 0
    override def toString() = s
  }

  
}

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



trait BasePath {
  type ThisType <: BasePath

  def /(chunk: PathChunk): ThisType

  def relativeTo(target: ThisType): RelPath

  def startsWith(target: ThisType): Boolean

  def endsWith(target: RelPath): Boolean
  
  def ext: String

  def baseName: String
}

trait BasePathImpl extends BasePath {
  def /(chunk: PathChunk): ThisType

  def ext = {
    val li = last.lastIndexOf('.')
    if(li == -1) ""
    else last.slice(li + 1,last.length())
  }

  override def baseName: String = {
    val li = last.indexOf('.')
    if(li == -1) last
    else last.slice(0,li) 
  }

  def last:String
}

trait SegmentedPath extends BasePath {
  protected[this] def make(p: Seq[String], ups: Int): ThisType
  def segments: IndexedSeq[String]
  def /(chunk: PathChunk) = make(
    segments.dropRight(chunk.ups) ++ chunk.segments,
    math.max(chunk.ups - segments.length, 0)
  )
  def endsWith(target: RelPath): Boolean = {
    this == target || (target.ups == 0 && this.segments.endsWith(target.segments))
  }
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

  def chunkify(s:RawPath):js.Array[String] = s.toSegment()
  


}

sealed trait FilePath extends BasePath {
  def toRaw: RawPath
  def resolveFrom(base: Path): Path
}

object FilePath {
  def apply[T: PathConvertible](f0: T) = {
    val f = implicitly[PathConvertible[T]].apply(f0)
    Path(f0)
  }
}

class RelPath private[os] (segments0: Array[String], val ups: Int) 
      extends FilePath with BasePathImpl with SegmentedPath {
   type ThisType = RelPath
  def last: String = segments.last
  def segments: IndexedSeq[String] = segments0
 
  require(ups >= 0)
  
  protected[this] def make(p: Seq[String], ups: Int) = {
    new RelPath(p.toArray[String], ups + this.ups)
  }

  override def relativeTo(base: RelPath): RelPath = {
     if (base.ups < ups) new RelPath(segments0, ups + base.segments.length)
     //else if (base.ups == ups) SubPath.relativeTo0(segments0, base.segments)
     //else throw PathError.NoRelativePath(this, base)
     ???
  }

  override def startsWith(target: ThisType): Boolean = {
    this.segments0.startsWith(target.segments) && this.ups == target.ups
  }

  override def toString = (Seq.fill(ups)("..") ++ segments0).mkString("/")

  override def toRaw: RawPath = new RawPath(toString())

  override def resolveFrom(base: Path): Path = base / this

  override def hashCode = segments.hashCode() + ups.hashCode()

  
}

object RelPath {
  val rel: RelPath = new RelPath(Internals.emptyStringArray, 0)
}

class Path private[os](val wrapped: RawPath) {
  type ThisType = Path
  override def toString(): String = wrapped.toString

  def last():String = wrapped.getFileName()
  
  def /(chunk: PathChunk): ThisType = {
    if (chunk.ups > wrapped.getNameCount) throw PathError.AbsolutePathOutsideRoot
    val resolved = wrapped.join(chunk.toString())
    new Path(resolved)
  }

  override def hashCode = wrapped.hashCode()

  def startsWith(target: Path) = wrapped.startsWith(target)

  def endsWith(target: RelPath) = wrapped.endsWith(target)

  def resolveFrom(base: Path) = this

  def segmentCount = wrapped.getNameCount

  def getSegment(i: Int): String = wrapped.getSegment(i).toString
}

object Path {
  def apply[T: PathConvertible](f0: T): Path = {
    val f = implicitly[PathConvertible[T]].apply(f0)
    new Path(f)
  }

  def apply(p: FilePath, base: Path) = p match{
    case p: RelPath => base / p
    case p: Path => p
    
  }

  implicit val pathOrdering: Ordering[Path] = new Ordering[Path]{
    def compare(x: Path, y: Path): Int = {
      val xSegCount = x.segmentCount
      val ySegCount = y.segmentCount
      if (xSegCount < ySegCount) -1
      else if (xSegCount > ySegCount) 1
      else if (xSegCount == 0 && ySegCount == 0) 0
      else{
        var xSeg = ""
        var ySeg = ""
        var i = -1
        while ({
          i += 1
          xSeg = x.getSegment(i)
          ySeg = y.getSegment(i)
          i < xSegCount && xSeg == ySeg
        }) ()
        if (i == xSegCount) 0
        else Ordering.String.compare(xSeg, ySeg)
      }
    }
  }
}

sealed trait PathConvertible[T] {
  def apply(t: T): RawPath
}

object PathConvertible {
  implicit object StringConvertible extends PathConvertible[String] {
    def apply(path: String): RawPath = RawPath.fromString(path)
  }
  implicit object RawPathConvertible extends PathConvertible[RawPath]{
    def apply(t:RawPath) = t
  }
}
