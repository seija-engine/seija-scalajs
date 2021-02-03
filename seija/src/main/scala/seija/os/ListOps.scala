package seija.os
import scalajs.js;
object list extends Function1[Path, IndexedSeq[Path]] {
  def apply(src: Path) = apply(src, true)

  def apply(src: Path, sort: Boolean = true): IndexedSeq[Path] = {
      val fsList = Foreign.listDir(src.toString())
      val arr = fsList.map(Path(_)).toArray
      if(sort) arr.sorted
      else arr
  }

  def roots():js.Array[Path] = {
    Foreign.getRoots().map(Path(_))
  }
}