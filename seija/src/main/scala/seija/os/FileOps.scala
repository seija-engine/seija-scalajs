package seija.os

object makeDir extends Function1[Path, Unit] {
  def apply(path: Path): Unit = Foreign.createDirectory(path.toString())

  object all extends Function1[Path, Unit] {
    def apply(path: Path): Unit = apply(path, true)
    def apply(path: Path, acceptLinkedDirectory: Boolean = true): Unit = {
        Foreign.createDirectory(path.toString)
    }
  }
}


object move {

}
