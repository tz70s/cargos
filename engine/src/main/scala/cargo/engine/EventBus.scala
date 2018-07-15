package cargo.engine

import akka.actor.{Actor, ActorRef, Props}
import cargo.Logging
import spray.json.JsValue

object EventBus {
  case class EventJson(content: JsValue)
  def props(name: String, from: String, to: List[ActorRef]): Props = Props(new EventBus(name, from, to))
}

class EventBus(val name: String, val from: String, val to: List[ActorRef]) extends Actor with Logging {
  import EventBus._

  override def preStart(): Unit = {
    log.debug(s"start event bus - $name")
  }

  override def postStop(): Unit = {
    log.debug(s"stop event bus - $name")
  }

  override def receive: Receive = {
    case EventJson(c) =>
      to.foreach { t =>
        val strip = t.path.name.substring("sink-".length).split("-")(0)
        log.debug(s"route event ${c.compactPrint}, from $from to $strip")
        t ! EventJson(c)
      }
  }
}
