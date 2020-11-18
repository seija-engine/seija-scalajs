package seija.ui
import seija.ui.{ Rect2DUIComp, ImageRenderUIComp,TransformUIComp, UIComponent, UITemplate};
object UISystem {
    def init():Unit  = {
         Control.init()
         UIComponent.register("Transform",new TransformUIComp)
         UIComponent.register("Rect2D",new Rect2DUIComp)
         UIComponent.register("ImageRender",new ImageRenderUIComp)

        Control.register("Image",Image.create)
    }
}