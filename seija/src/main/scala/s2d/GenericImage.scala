package s2d
import data.Color;
import s2d.ImageType;
trait GenericImage[T] {
    var _color:Color = Color.NewCB(1f,1f,1f,1f,colorToRust); 
    
    def color_= (v:Color) = {
        _color = v
        _color.setCallback(colorToRust)
        this.colorToRust()
    }

    def setImageType(typ:ImageType):Unit = {}

    def colorToRust():Unit
}