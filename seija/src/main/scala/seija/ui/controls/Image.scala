package seija.ui.controls
import seija.ui.Control
import seija.ui.ControlCreator
import seija.ui.ControlParams
import seija.core.Entity
import seija.core.Transform
import seija.s2d.Rect2D
import seija.s2d.Transparent
import seija.s2d.ImageRender
import seija.s2d.layout.LayoutView
import seija.s2d.assets
import seija.data.Read._
import seija.ui.Utils
import scala.scalajs.js
import seija.data.Color
import seija.s2d.ImageType
import seija.ui.comps.LayoutViewComp

object Image {
    implicit val imageCreator:ControlCreator[Image] = new ControlCreator[Image] {
        val name: String = "Image"
        def init(): Unit = {}
        def create(): Image = new Image
    }
}

class Image extends Control with LayoutViewComp {
    override def init(parent: Option[Control], params: ControlParams,ownerControl:Option[Control] = None) {
        super.init(parent,params,ownerControl)
        val entity = Entity.New(parent.map(_.getEntity))
        this.entity = Some(entity)
        entity.addComponent[Transform]()
        val rect = entity.addComponent[Rect2D]()
        entity.addComponent[Transparent]()
        val imageRender = entity.addComponent[ImageRender]()
        val view = entity.addComponent[LayoutView]()
        this.addPropertyLister[Int]("texture",(texId) => {
            imageRender.setTexture(new assets.Image(texId))
        })
        this.addPropertyLister[Color]("color",(color) => {
            imageRender.color = color
        })
        this.addPropertyLister[ImageType]("imageType",(typ) => {
            imageRender.setImageType(typ)
        })
        this.addPropertyLister[Float]("fillValue",(value) => {
            imageRender.setFilledValue(value)
        })
        this.initProperty[Int]("texture",params.paramStrings,None)
        this.initProperty[Color]("color",params.paramStrings,None)
        this.initProperty[ImageType]("imageType",params.paramStrings,None)
        this.initProperty[Float]("fillValue",params.paramStrings,None)
        this.initLayoutView(this,view,params)
    }
}