package seija.ui2
import seija.core.Entity
import seija.data.{Read, SBool, SContent, SExpr, SExprInterp, SFloat, SFunc, SInt, SKeyword, SList, SNFunc, SNil, SObject, SString, SSymbol, SUserData, SVector}
import seija.math.Vector3
import seija.ui2.UIComponent.cacheContent

import scala.scalajs.js.Dictionary
import scalajs.js
import seija.data.XmlNode

class Control extends IBehavior {
    var nsDic:js.Dictionary[String] = js.Dictionary()
    private var _parent:Option[Control] = None
    def parent: Option[Control] = _parent
    val sContent:SContent = new SContent(UISystem.cContent)
    var entity:Option[Entity] = None
    var property:js.Dictionary[Any] = js.Dictionary()
    val propertyListenerDic: Dictionary[js.Array[Any => Unit]] = js.Dictionary()
    var template:Option[UITemplate] = None
    var dataContent:Option[Any] = None
    var evProperty:js.Dictionary[(Boolean,() => Unit)] = js.Dictionary()
    var eventBoard:Option[EventBoard] = None

    val childs:js.Array[Control] = js.Array()


    def init():Unit = {
      this.sContent.set("control",SUserData(this))
      if(this.template.isDefined) {
        this.template.get.create() match {
          case Left(value) => println(value)
          case Right(value) => this.entity = Some(value)
        }
      }
      if(this.parent.isDefined) {
        this.entity.get.setParent(this.parent.get.entity)
      }
    }

    def OnEnter():Unit = {
      this.childs.foreach(_.OnEnter())
    }

    def setParent(parent:Option[Control]):Unit = {
      this._parent = parent
      if(parent.isDefined) {
        if(this.eventBoard.isEmpty) {
            this.eventBoard = parent.get.eventBoard
        }
        parent.get.childs.push(this)
      }
      
    }

    def setParams(params:js.Dictionary[String]):Unit = {}
    def setTemplates(tmpls:js.Dictionary[XmlNode]):Unit = {}

    protected def setParam[T](name:String,dic:js.Dictionary[String],defValue:Option[T])(implicit readT:Read[T]):Unit = {
      dic.get(name) match {
        case Some(paramString) =>
          Utils.parseParam(paramString) match {
            case Left(paramString) =>
              if(readT.read(paramString).map(v => this.property.put(name,v)).isEmpty) {
                println(s"property error ${name}:${paramString}")
              }
            case Right(expr) => this.setLispParam(name,dic,defValue)
          }
        case None =>
          if(defValue.isDefined) {
            this.property.put(name,defValue.get)
          }
      }
    }

    protected def setLispParam[T](name:String,dic:js.Dictionary[String],defValue:Option[T]):Unit = {
      val paramString = dic.get(name)
      if(paramString.isDefined) {
        cacheContent.parent = Some(this._parent.get.sContent)
        cacheContent.set("setFunc",SUserData(v => this.setProperty(name,v) ))
        val retValue = SExprInterp.evalStringToValue(paramString.get, Some(cacheContent))
        this.setProperty(name,retValue)
      } else if(defValue.isDefined) {
        this.property.put(name,defValue.get)
      }
    }

    protected def setEventParam(name:String,params:js.Dictionary[String]):Unit = {
      val paramString = params.get(name)
      if(paramString.isEmpty) return
      val expr = Utils.parseParam(paramString.get)
      if(expr.isLeft) return
      val curContent = this._parent.map(_.sContent)
      UIComponent.cacheContent.clear()
      UIComponent.cacheContent.parent = curContent
      SExprInterp.eval(expr.getOrElse(null)) match {
        case SVector(list) =>
          val isCapture = list(0).asInstanceOf[SBool].value
          val f = list(1).asInstanceOf[SFunc]
          this.evProperty.put(name,(isCapture,() => f.call(curContent)))
        case f@SFunc(_, _) =>
          this.evProperty.put(name,(false,() => f.call(curContent)))
        case _ => ()
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