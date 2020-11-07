package seija.ui

import seija.core.event.EventNode
import seija.core.{Entity, Template}
import seija.s2d.SpriteRender
import seija.core.event._

import scalajs.js
class CheckBox extends Control {
  var isChecked:Boolean = false
  var enable:Boolean = true

  var checkSprite:Option[SpriteRender] = None
  def onStart(parent:Entity):Unit = {
    Template.fromXmlFile("/checkBox.xml") match {
      case Left(value) => println(value)
      case Right(tmpl) =>
        val (e,idRef) = tmpl.call(js.Dictionary())
        e.setParent(Some(parent))
        val entity = idRef.find("checkSprite")
        this.checkSprite = entity.get.getComponent[SpriteRender]()
        val eventNode = entity.get.addComponent[EventNode]()
        eventNode.register(GameEventType.Click,isCapture = false,this.onClickCheckBox)
    }

  }

  var testCount = 0;
  def onClickCheckBox():Unit = {
    if(this.enable) {
      this.testCount += 1;
      this.isChecked = !this.isChecked;
      this.checkSprite.get.setSpriteName(if(isChecked) "checkbox-checked" else "checkbox-unchecked")
      if(this.testCount > 2) {
        this.setEnable(false)
      }
    }
  }

  def setEnable(b:Boolean):Unit = {
    this.enable = b;
    val sprName = if(isChecked) "checkbox-checked-disabled" else "checkbox-unchecked-disabled";
    this.checkSprite.get.setSpriteName(sprName)
  }



}
