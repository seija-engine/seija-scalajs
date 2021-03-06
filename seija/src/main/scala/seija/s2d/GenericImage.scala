package seija.s2d

import seija.data.Color
import slogging.LazyLogging;
trait GenericImage[T] extends LazyLogging {
    var _color:Color = Color.NewCB(1f,1f,1f,1f,colorToRust);
    var _imageType:ImageType = ImageSimple
    
    def color_= (v:Color): Unit = {
        _color = v
        _color.setCallback(colorToRust)
        this.colorToRust()
    }

    def color:Color = {
        this.colorFromRust()
        _color
    }

    def imageType:ImageType = _imageType
    def setImageType(typ:ImageType):Unit = {}

    def setFilledValue(v:Float) :Unit = {}

    def colorToRust():Unit
    def colorFromRust():Unit
}