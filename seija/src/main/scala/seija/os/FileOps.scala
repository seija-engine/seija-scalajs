package seija.os

object makeDir extends Function1[Path, Unit] {
  def apply(path: Path): Unit = Foreign.createDirectory(path.toString(),false)

  object all extends Function1[Path, Unit] {
    def apply(path: Path): Unit = apply(path, true)
    def apply(path: Path, acceptLinkedDirectory: Boolean = true): Unit = {
        if(isDir(path) && isLink(path) && acceptLinkedDirectory) () 
        else Foreign.createDirectory(path.toString,true)
    }
  }
}


object move {

}
