package seija.os

import scala.scalajs.js;

class RawPath(val path:String,private var seqs:Option[js.Array[String]] = None) {
    override def toString(): String = this.path

    private def updateSeqs() = this.seqs match {
        case Some(value) => ()
        case None => this.seqs = Some(Foreign.splitPath(path))
    }
    def toSegment():js.Array[String] = {
        this.updateSeqs();
        this.seqs.get
    }

    def getSegment(i:Int):String = {
        this.seqs.get(i)
    }

    def getNameCount:Int = {
        this.updateSeqs();
        this.seqs.get.length
    }

    def getFileName():String = {
        Foreign.getFileName(this.path)
    }

    def join(other:String):RawPath = {
        new RawPath(Foreign.joinPath(this.path,other))
    }

    def startsWith(target: Path) = Foreign.startsWith(this.path,target.toString())

    def endsWith(target: RelPath) = Foreign.endsWith(this.path,target.toString())
}

object RawPath {
    def fromString(path:String):RawPath = new RawPath(path)
}