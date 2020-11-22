package seija.ui2
import seija.core.Entity
import seija.data.{Read, SContent, SExpr, SExprInterp, SList, SUserData, SVector}
import seija.math.Vector3
import seija.ui2.UIComponent.cacheContent

import scala.scalajs.js.Dictionary
import scalajs.js

class Control extends IBehavior {
    var nsDic:js.Dictionary[String] = js.Dictionary()
    var parent:Option[Control] = None
    val sContent:SContent = new SContent(UISystem.cContent)
    var entity:Option[Entity] = None
    var property:js.Dictionary[Any] = js.Dictionary()
    val propertyListenerDic: Dictionary[js.Array[Any => Unit]] = js.Dictionary()
    var template:Option[UITemplate] = None
    var dataContent:Option[Any] = None
    var evBindDic:Dictionary[js.Array[(SExpr => Unit)]] = js.Dictionary()


    override def handleEvent(evData: js.Array[SExpr]): Unit = {
      parent.foreach(_.handleEvent(evData))
      this.dispatchEvent(evData(0).castKeyword(),SVector(evData.tail))
    }

    def init():Unit = {
      this.sContent.set("control",SUserData(this))
    }

    def setParams(params:js.Dictionary[String]):Unit = {}

    protected def setParam[T](name:String,dic:js.Dictionary[String],defValue:Option[T])(implicit readT:Read[T]):Unit = {
      dic.get(name) match {
        case Some(paramString) =>
          Utils.parseParam(paramString) match {
            case Left(paramString) =>
              if(readT.read(paramString).map(v => this.property.put(name,v)).isEmpty) {
                println(s"property error ${name}:${paramString}")
              }
            case Right(expr) =>
              cacheContent.parent = Some(this.parent.get.sContent)
              cacheContent.set("setFunc",SUserData(v => this.setProperty(name,v) ))
              SExprInterp.eval(expr, Some(cacheContent)) match {
                case SUserData(value) => this.setProperty(name,value)
                case _ => ()
              }
          }
        case None =>
          if(defValue.isDefined) {
            this.property.put(name,defValue.get)
          }
      }
      dic.get(name).map(Utils.parseParam).foreach {
        case Left(value) =>
          readT.read(value) match {
          case Some(value) => this.property.put(name,value)
          case None => println(s"property error ${name}")
        }
        case Right(value) =>
      }
    }

    def Enter():Unit = {
       if(this.template.isDefined) {
           this.template.get.create() match {
               case Left(value) => println(value)
               case Right(value) => this.entity = Some(value)
           }
       }
    }

    def setProperty(name:String,value:Any):Unit = {
      this.property.update(name,value)
      this.dispatchPropertyChange(name)
    }

    def dispatchPropertyChange(name:String):Unit = {
      val mayLst = this.propertyListenerDic.get(name)
      if(mayLst.isDefined) {
        val value = this.property(name)
        mayLst.get.foreach(_(value))
      }
    }

    override def emit(evKey:String,evData:SExpr): Unit = {
      this.dispatchEvent(evKey,evData)
    }

    def dispatchEvent(evKey:String,evData:SExpr):Unit = {
      val mayList = this.evBindDic.get(evKey)
      mayList.foreach(arr => {
        arr.foreach(f => f(evData))
      })
    }

    def addEvent(evKey:String,f:SExpr => Unit):Unit = {
      if(!this.evBindDic.contains(evKey)) {
        this.evBindDic.put(evKey,js.Array())
      }
      this.evBindDic(evKey).push(f)
    }

    def addPropertyListener(propertyName:String,f:Any => Unit):Unit = {
      if( this.property.contains(propertyName)) {
        if(!this.propertyListenerDic.contains(propertyName)) {
          this.propertyListenerDic.put(propertyName,js.Array(f))
        } else {
          this.propertyListenerDic(propertyName).push(f)
        }
      }
    }
}