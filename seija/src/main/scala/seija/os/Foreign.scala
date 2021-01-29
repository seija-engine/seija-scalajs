package seija.os

import seija.core.Deno

object Foreign {
    def root:String = Deno.core.jsonOpSync("fs_root").asInstanceOf[String]
    def home:String = Deno.core.jsonOpSync("fs_home").asInstanceOf[String]
    def pwd:String = Deno.core.jsonOpSync("fs_pwd").asInstanceOf[String]

    def createDirectory(path:String) = ???
    def currentDir():String = ???
}