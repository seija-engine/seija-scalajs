package demo

import assets.Loader
import core.{Entity, Transform}
import s2d.{ImageRender, Rect2D, SpriteRender, TextRender, Transparent}
import s2d.assets.{Font, Image, SpriteSheet}

object BaseRenderComps {
  def show():Unit = {
    var font = Loader.loadSync[Font]("WenQuanYiMicroHei.ttf").toOption.get;
    var paperSheet = Loader.loadSync[SpriteSheet]("paper.json").toOption.get;
    var starTexture = Loader.loadSync[Image]("StarIcon.png").toOption.get;

    this.createImage(starTexture,-400,300)
    this.createLabel(font,"Simple Texture",-400,230)

    this.createSprite(paperSheet,"BlueButton",260,176,-200,300)
    this.createLabel(font,"Simple Sprite",-200,230)

    var sprite = this.createSprite(paperSheet,"BlueButton",660,176,100,300)
    this.createLabel(font,"Slice Sprite",100,230)
    sprite.setImageType(s2d.ImageSliced(170,50,10,10))

    var sprite2 = this.createSprite(paperSheet,"StarIcon",176,176,400,300)
    this.createLabel(font,"Filled Sprite",400,230)
    sprite2.setImageType(s2d.ImageFilled(s2d.ImageFilledType.HorizontalLeft,0.6f))


    this.createLabel(font,"Single LineText",0,100)
    var text = this.createLabel(font,"Single LineText Anchor Left",0,50,300)
    text.setAnchor(data.AnchorAlign.Left)

    this.createLabel(font,"Single LineText",0,100)
    var text2 = this.createLabel(font,"Single LineText Anchor Right",0,0,300)
    text2.setAnchor(data.AnchorAlign.Right)

    val longText = "彼岸的世界的观念是起自柏拉图，彼岸的世界是建立在线性时间观上的，而线性时间观是由基督教提出并影响现世的。一切始于亚当和夏娃偷吃了禁果，一切终于最后的审判。 最后的审判是一《圣经》中启示录的预言，在世界末日之时真神耶稣基督会从天上而临，耶稣基督会将死者复生并对他们进行审判，恶人将会被丢入硫磺火湖中永远灭亡。善者将会上天堂。一切的一切 将会到此时得到结算和偿还。而一切将在此时结束。此岸世界不值得过，此岸世界只是暂时的，而一切都有一个最终的目的，这个目的是神圣的。";

    this.createLabel(font,"Single LineText",0,100)
    var text3 = this.createLabel(font,"多行文本自动换行\r\r"+longText,-400,-100,800,16)
    text3.setAnchor(data.AnchorAlign.TopLeft)
    text3.setLineMode(s2d.LineMode.Wrap)
  }


  def createImage(tex:Image,x:Int,y:Int):Unit = {
    var entity = Entity.New();
    var t = entity.addComponent[Transform]();
    t.localPosition.set(x,y,100)
    var rect2d = entity.addComponent[Rect2D]();
    rect2d.size.set(100,100)
    entity.addComponent[Transparent]();
    var image = entity.addComponent[ImageRender]();
    image.setTexture(tex)
  }

  def createSprite(sheet:SpriteSheet,spriteName:String,w:Int,h:Int,x:Int,y:Int,scale:Float = 0.5f,z:Int = 100):SpriteRender = {
    var entity = Entity.New();
    var t = entity.addComponent[Transform]();
    t.localPosition.set(x,y,z)
    t.scale.set(scale,scale,1)
    var rect2d = entity.addComponent[Rect2D]();
    rect2d.size.set(w,h);
    entity.addComponent[Transparent]();
    var sprite = entity.addComponent[SpriteRender]();
    sprite.setSpriteSheet(sheet)
    sprite.setSpriteName(spriteName)

    sprite.setSliceByConfig(0)

    sprite;
  }

  def createLabel(font:Font,txt:String,x:Int,y:Int,width:Int = 120,fontSize:Int = 24):TextRender = {
    var entity = Entity.New();
    var t = entity.addComponent[Transform]();
    t.localPosition.set(x,y,2);
    var rect = entity.addComponent[Rect2D]();
    rect.size.set(width,200);
    entity.addComponent[Transparent]();
    var text = entity.addComponent[TextRender]();
    text.setFont(font)
    text.setFontSize(fontSize)
    text.color.set(1,1,1,1)

    text.setText(txt)
    text
  }
}
