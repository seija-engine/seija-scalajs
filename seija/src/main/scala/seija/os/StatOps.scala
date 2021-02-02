package seija.os

object isDir extends Function1[Path, Boolean]{
  def apply(p: Path): Boolean = Foreign.isDir(p.toString())
}

object isLink extends Function1[Path, Boolean]{
  def apply(p: Path): Boolean = Foreign.isLink(p.toString())
}