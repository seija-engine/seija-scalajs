package s2d
import data.Color;
import s2d.ImageType;
trait GenericImage[T] {
    var _color:Color = Color.NewCB(1f,1f,1f,1f,colorToRust); 
    
    def color_= (v:Color): Unit = {
        _color = v
        _color.setCallback(colorToRust)
        this.colorToRust()
    }

    def color:Color = {
        this.colorFromRust()
        _color
    }

    def setImageType(typ:ImageType):Unit = {}

    def colorToRust():Unit
    def colorFromRust():Unit
}