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
    override def OnInit(parent: Option[Control], params: ControlParams,ownerControl:Option[Control] = None) {
        val entity = this.entity.get
        entity.addComponent[Transform]()
        val rect = entity.addComponent[Rect2D]()
        entity.addComponent[Transparent]()
        val imageRender = entity.addComponent[ImageRender]()
        val view = entity.addComponent[LayoutView]()
        
        this.initProperty[Int]("texture",params.paramStrings,None,Some((texId) => {
            imageRender.setTexture(new assets.Image(texId))
        }))
        this.initProperty[Color]("color",params.paramStrings,None,Some((color) => {
            imageRender.color = color
        }))
        this.initProperty[ImageType]("imageType",params.paramStrings,None,Some((typ) => {
            imageRender.setImageType(typ)
        }))
        this.initProperty[Float]("fillValue",params.paramStrings,None,Some((value) => {
           imageRender.setFilledValue(value)
        }))
        this.initLayoutView(this,view,params)
    }
}