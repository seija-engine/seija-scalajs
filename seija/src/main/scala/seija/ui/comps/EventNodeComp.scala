package seija.ui.comps
import scalajs.js
import seija.ui.Control
import seija.ui.ControlParams
import seija.core.event.EventNode
import seija.core.event.GameEventType
import seija.data.SExprInterp
import slogging.LazyLogging
import seija.data.SFunc

trait EventNodeComp extends LazyLogging {
    def initEventComp(control:Control,param:ControlParams) {
        val onEvents:js.Dictionary[String] = param.paramStrings.filter(_._1.startsWith("On"))
        val entity = control.entity.get
        var eventNode:Option[EventNode] = None
        if(!onEvents.isEmpty) {
          eventNode = Some(entity.addComponent[EventNode]()) 
        }
        for((k,v) <- onEvents) {
           val evType = GameEventType.gameEventTypeRead.read(k.substring(2))
           if(evType.isEmpty) {
             logger.error(s"$k is not EventType")
           } else {
             val sFunc = SExprInterp.evalString(v,Some(control.sContext))
             sFunc match {
                case Left(value) => logger.error(value)
                case Right(f@SFunc(_,_)) if evType.isDefined =>
                  eventNode.get.register(evType.get,false,() => {
                       f.call(Some(control.sContext))
                })
                case _ => () 
             }
           }
        }
    }
}