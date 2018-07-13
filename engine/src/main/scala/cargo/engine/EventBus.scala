package cargo.engine

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import cargo.engine.EventBus.{EventJson, EventObject}
import spray.json.JsValue

object EventBus {
  case class EventObject(content: String)
  case class EventJson(content: JsValue)
  def props(name: String, to: List[ActorRef]): Props = Props(new EventBus(name, to))
}

class EventBus(val name: String, val to: List[ActorRef]) extends Actor with ActorLogging {

  override def receive: Receive = {
    case EventObject(c) =>
      // TODO: parsing the routing rules.
      to.foreach(_ ! EventObject(c))
    case EventJson(c) =>
      log.info("Reroute event $c")
      to.foreach(_ ! EventJson(c))
  }
}
