package seija.os
import scalajs.js;
import seija.core.Deno
import slogging.LazyLogging

object Foreign extends LazyLogging {
    def root:String = Deno.core.jsonOpSync("fsRoot").asInstanceOf[String]
    def home:String = Deno.core.jsonOpSync("fsHome").asInstanceOf[String]
    def pwd:String = Deno.core.jsonOpSync("fsPwd").asInstanceOf[String]
    def splitPath(str:String):js.Array[String] = Deno.core.jsonOpSync("fsSplitPath",str).asInstanceOf[js.Array[String]];
    def joinPath(path:String,joinPath:String):String = Deno.core.jsonOpSync("fsJoinPath",js.Array(path,joinPath)).asInstanceOf[String]
    def startsWith(path:String,str:String):Boolean = Deno.core.jsonOpSync("fsStartsWith",js.Array(path,str)).asInstanceOf[Boolean]
    def endsWith(path:String,str:String):Boolean = Deno.core.jsonOpSync("fsEndWith",js.Array(path,str)).asInstanceOf[Boolean]
    def hasRoot(path:String):Boolean = Deno.core.jsonOpSync("fsHasRoot",path).asInstanceOf[Boolean]
    def getFileName(path:String):String = Deno.core.jsonOpSync("fsGetFileName",path).asInstanceOf[String]
    def isDir(path:String):Boolean = Deno.core.jsonOpSync("fsIsDir",path).asInstanceOf[Boolean]
    def isLink(path:String):Boolean = Deno.core.jsonOpSync("fsIsLink",path).asInstanceOf[Boolean]
    def listDir(path:String):js.Array[String] = Deno.core.jsonOpSync("fsListDir",path).asInstanceOf[js.Array[String]]

    def createDirectory(path:String,isAll:Boolean):Boolean = {
        val fName = if(isAll) "fsCreateDirAll" else "fsCreateDir";
        val t = Deno.core.jsonOpSync(fName,path)
        if(t.isInstanceOf[Boolean]) {
           true
        } else {
           logger.error(t.asInstanceOf[String])
           false
        }
    }
}