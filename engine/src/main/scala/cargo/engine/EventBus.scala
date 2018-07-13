package cargo.engine

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import spray.json.JsValue

object EventBus {
  case class EventJson(content: JsValue)
  def props(name: String, to: List[ActorRef]): Props = Props(new EventBus(name, to))
}

class EventBus(val name: String, val to: List[ActorRef]) extends Actor with ActorLogging {
  import EventBus._

  override def postStop(): Unit = {
    log.debug(s"stop event bus - $name")
  }

  override def receive: Receive = {
    case EventJson(c) =>
      log.debug(s"route event ${c.toString()}")
      to.foreach(_ ! EventJson(c))
  }
}
