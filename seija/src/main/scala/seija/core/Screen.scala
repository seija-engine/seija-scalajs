package seija.core

object Screen {
    private var _width:Int = 0
    private var _height:Int = 0
    def width:Int = _width
    def height:Int = _height

    private[seija] def init(w:Int,h:Int) {
        _width = w;
        _height = h;
    }

    def setSize(w:Int,h:Int) = {
        _width = w;
        _height = h;
    }
}