package seija.os

class RawPath(val path:String) {
    override def toString(): String = this.path
}

object RawPath {
    def fromString(path:String):RawPath = new RawPath(path)
}