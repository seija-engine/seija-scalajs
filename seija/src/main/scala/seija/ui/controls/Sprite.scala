package seija.ui.controls

import seija.ui.Control
import seija.ui.comps.LayoutViewComp
import seija.ui.comps.EventNodeComp
import seija.ui.ControlCreator
import seija.ui.ControlParams
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.Transparent
import seija.s2d.SpriteRender
import seija.s2d.layout.LayoutView
import seija.s2d.assets.SpriteSheet
import seija.s2d.ImageType
import seija.data.Color

object Sprite {
    implicit val spriteCreator:ControlCreator[Sprite] = new ControlCreator[Sprite] {
        val name: String = "Sprite"
        def init(): Unit = {}
        def create(): Sprite = new Sprite
    }
}

class Sprite extends Control with LayoutViewComp with EventNodeComp {
    override def OnInit(parent: Option[Control], params: ControlParams, ownerControl: Option[Control]): Unit = {
        val entity = this.entity.get
        entity.addComponent[Transform]()
        entity.addComponent[Rect2D]()
        entity.addComponent[Transparent]()
        val spriteRender = entity.addComponent[SpriteRender]()
        val view = entity.addComponent[LayoutView]()
        this._view = Some(view)

        this.initProperty[Int]("sheet",params.paramStrings,None,Some((sheetID) => {
            spriteRender.setSpriteSheet(new SpriteSheet(sheetID))
        }));

        this.initProperty[String]("spriteName",params.paramStrings,None,Some((spriteName) => {
            spriteRender.setSpriteName(spriteName)
        }))
        this.initProperty[ImageType]("type",params.paramStrings,None,Some((typ) => {
            spriteRender.setImageType(typ)
        }))
        this.initProperty[Float]("fillValue",params.paramStrings,None,Some((value) => {
           spriteRender.setFilledValue(value)
        }))
         this.initProperty[Color]("color",params.paramStrings,None,Some((color) => {
            spriteRender.color = color
        }))

        initLayoutView(this,view,params)
        initEventComp(this,params)
    }
}