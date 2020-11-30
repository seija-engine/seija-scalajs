package seija.data
import scalajs.js
import scala.collection.mutable
import scala.reflect.ClassTag
import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scala.annotation.StaticAnnotation

class DynClass extends StaticAnnotation {
  def macroTransform(annottees: Any*):Any = macro DynClassMacro.impl
}

object DynClassMacro {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    println(annottees.head.staticType)
    annottees.map(_.tree).toList match {
      case (obj: ClassDef) :: Nil =>
        //??? error
        println("rrrrrrrr:" + obj.tpe)
        println("cccccc:"+ obj.symbol)
        //val check = c.typecheck(obj)
        //println(check)
        //val name = c.typecheck(obj).symbol.fullName
        //println(name)
    }
    annottees.head
  }
}



class DynObject {
  var handleDic:Option[js.Dictionary[Any => Any]] = None
  var handleFunc:Option[(String,Any) => Any] = None

  def initClass():Unit = {
    this.handleDic = Some(js.Dictionary())
    this.handleFunc = Some((s,v) => {
      this.handleDic.get(s)(v)
    })
  }

  def addAttrFunc(name:String,f:Any => Any):Unit = {
    this.handleDic.get.put(name,f)
  }
}


object DynObject {
    var dynMap:mutable.HashMap[String,DynObject] = mutable.HashMap()

    def init():Unit = {
        //HashMap
        val hashObject = new DynObject
        hashObject.handleFunc = Some((key,map) => {
            map.asInstanceOf[mutable.HashMap[String,Any]](key)
        })
        this.registerType(classOf[mutable.HashMap[_, _]].getName,hashObject)
        val imHashObject = new DynObject
        imHashObject.handleFunc = Some((key,map) => {
            map.asInstanceOf[scala.collection.immutable.HashMap[String,Any]](key)
        })
        this.registerType(classOf[scala.collection.immutable.HashMap[_, _]].getName,hashObject)

        
    }

    def registerType(key:String,dynObject:DynObject):Unit = {
        this.dynMap.put(key,dynObject)
    }

    def registerClass[T]():Unit = macro registerClassImpl[T]

    def registerClassImpl[T:c.WeakTypeTag](c:whitebox.Context)():c.Tree = {
        import c.universe._
        val typeSym = weakTypeOf[T].typeSymbol
        val typeName = typeSym.fullName
        val nameList = weakTypeOf[T].decls.collect {
          case m: MethodSymbol if m.isAccessor && !m.fullName.exists(_ == '$') => m.name
        }.toList
        var allList:List[c.Tree] = List()
        for(attrName <- nameList) {
          val tpt = tq"$typeSym"
          val attrQ =
            q"""
              dynObject.addAttrFunc(${attrName.toString},(a:Any) => a.asInstanceOf[$tpt].$attrName)
             """
          allList = allList :+ attrQ
        }
        val head = q"""
          val dynObject = new DynObject
          dynObject.initClass()
          DynObject.registerType($typeName,dynObject)
          ..${allList}
        """
        head
    }

    def findValue(path:String,value:Any):Option[Any] = {
        val arr = path.split('.')
        if(arr(0) != "data") return None
        var curValue = value
        for(name <- arr.tail) {
           val typeName = curValue.getClass.getName
           this.dynMap.get(typeName) match {
               case Some(dynObject) => 
                curValue = dynObject.handleFunc.get(name,curValue)
               case None => 
                  println(s"$typeName not register in DynObject")
                  return None
           }
        }
        Some(curValue)
    }
}