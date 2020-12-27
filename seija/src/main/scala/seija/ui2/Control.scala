package seija.ui2

import seija.core.Entity
import seija.data._
import seija.ui2.UIComponent.cacheContent

import scala.scalajs.js
import scala.scalajs.js.Dictionary

class Control extends IBehavior {
  var nsDic: js.Dictionary[String] = js.Dictionary()
  private var _parent: Option[Control] = None

  def parent: Option[Control] = _parent

  val sContent: SContent = new SContent(UISystem.cContent)
  var entity: Option[Entity] = None
  var property: js.Dictionary[Any] = js.Dictionary()
  val propertyListenerDic: Dictionary[js.Array[Any => Unit]] = js.Dictionary()
  var template: Option[UITemplate] = None
  var dataContent: Option[Any] = None
  var evProperty: js.Dictionary[(Boolean, () => Unit)] = js.Dictionary()
  var eventBoard: Option[EventBoard] = None

  val children: js.Array[Control] = js.Array()


  def init(): Unit = {
    this.sContent.set("control", SUserData(this))
    if (this.template.isDefined) {
      this.template.get.create() match {
        case Left(value) => println(value)
        case Right(value) => this.entity = Some(value)
      }
    }
  }

  def OnEnter(): Unit = {
    this.children.foreach(_.OnEnter())
  }

  def addChild(control: Control): Unit = {
    this.children.push(control)
  }

  def removeChild(control: Control): Unit = {
    val index: Int = this.children.indexOf(control)
    if (index >= 0) {
      this.children.remove(index)
    }
  }

  override def handleEvent(evKey: String, evData: js.Array[SExpr]): Unit = {
    this.eventBoard.foreach(_.fire(evKey,evData))
  }

  def setParent(parent: Option[Control]): Unit = {
    if (_parent != parent) {
      if (_parent.isDefined) {
        _parent.get.removeChild(this)
      }
      if (parent.isDefined) {
        parent.get.addChild(this)
        if (this.eventBoard.isEmpty) {
          this.eventBoard = parent.get.eventBoard
        }
      }
      this._parent = parent
    }
  }

  def setProperty(name: String, value: Any): Unit = {
    this.property.update(name, value)
    this.dispatchPropertyChange(name)
  }

  def dispatchPropertyChange(name: String): Unit = {
    val mayLst = this.propertyListenerDic.get(name)
    if (mayLst.isDefined) {
      val value = this.property(name)
      mayLst.get.foreach(_ (value))
    }
  }

  def addPropertyListener(propertyName: String, f: Any => Unit): Unit = {
    if (this.property.contains(propertyName)) {
      if (!this.propertyListenerDic.contains(propertyName)) {
        this.propertyListenerDic.put(propertyName, js.Array(f))
      } else {
        this.propertyListenerDic(propertyName).push(f)
      }
    }
  }

  def setParams(params: js.Dictionary[String]): Unit = {}

  def setTemplates(temples: js.Dictionary[XmlNode]): Unit = {}

  def setParam[T](name: String, dic: js.Dictionary[String], defValue: Option[T])(implicit readT: Read[T]): Unit = {
    dic.get(name) match {
      case Some(paramString) =>
        Utils.parseParam(paramString) match {
          case Left(paramString) =>
            if (readT.read(paramString).map(v => this.property.put(name, v)).isEmpty) {
              println(s"property error ${name}:${paramString}")
            }
          case Right(_) => this.setLispParam(name, dic, defValue)
        }
      case None =>
        if (defValue.isDefined) {
          this.property.put(name, defValue.get)
        }
    }
  }

  def setLispParam[T](name: String, dic: js.Dictionary[String], defValue: Option[T]): Unit = {
    val paramString = dic.get(name)
    if (paramString.isDefined) {
      cacheContent.parent = Some(this._parent.get.sContent)
      cacheContent.set("setFunc", SUserData(v => this.setProperty(name, v)))
      val retValue = SExprInterp.evalStringToValue(paramString.get, Some(cacheContent))
      this.setProperty(name, retValue)
    } else if (defValue.isDefined) {
      this.property.put(name, defValue.get)
    }
  }

  def setEventParam(name: String, params: js.Dictionary[String]): Unit = {
    val paramString = params.get(name)
    if (paramString.isEmpty) return
    val expr = Utils.parseParam(paramString.get)
    if (expr.isLeft) return
    val curContent = this._parent.map(_.sContent)
    UIComponent.cacheContent.clear()
    UIComponent.cacheContent.parent = curContent
    SExprInterp.eval(expr.getOrElse(null)) match {
      case SVector(list) =>
        val isCapture = list(0).asInstanceOf[SBool].value
        val f = list(1).asInstanceOf[SFunc]
        this.evProperty.put(name, (isCapture, () => {
          f.call(curContent)
        }))
      case f@SFunc(_, _) =>
        this.evProperty.put(name, (false, () => {
          f.call(curContent)
        }))
      case _ => ()
    }
  }


  def destroy(): Unit = {
    this.setParent(None);
    if (this.entity.isDefined) {
      this.entity.get.destroy()
    }
  }
}