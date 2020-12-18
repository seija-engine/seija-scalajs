package seija.data
class ParseString(var string:String) {
    var chars:Array[Char] = string.toCharArray
    var curIndex:Int = -1

    def moveNext():Unit = {
        if(this.curIndex + 1 < this.chars.length) {
            this.curIndex += 1
        }
    }

    def lookNext(n:Int):Option[Char] = {
        if(this.chars.length > this.curIndex + n) {
            return Some(this.chars(this.curIndex + n))
        }
        None
    }

    def takeWhile(f:Char => Boolean):String = {
        var retString:String = ""
        do {
            this.lookNext(1) match {
                case None => return retString
                case Some(value) => 
                 if(f(value)) {
                     retString += value
                     this.moveNext()
                 } else {
                      return retString
                 }
            }
        } while(true)
        ""
    }

    def skipWhile(f:Char => Boolean):Unit = {
        do {
            this.lookNext(1) match {
                case None => return
                case Some(value) => 
                 if(f(value)) {
                     this.moveNext()
                 } else {
                      return
                 }
            }
        } while(true)
    }
   
}