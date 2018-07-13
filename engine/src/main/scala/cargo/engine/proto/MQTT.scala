package cargo.engine.proto

import java.util.UUID.randomUUID
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import cargo.engine.EventBus.{EventJson, EventObject}
import com.sandinh.paho.akka._

class MQTTSource(val name: String, val userPath: String, val bus: ActorRef)(implicit val system: ActorSystem)
    extends ProtocolSource[Unit] {

  private val splitted = userPath.split("@@")
  private val broker = splitted(0)
  private val topic = splitted(1)

  override def expose = {}

  private val mediator =
    system.actorOf(Props(classOf[MqttPubSub], PSConfig(brokerUrl = s"tcp://$broker", randomUUID().toString)))

  private val subscribeActor = system.actorOf(SubscribeActor.props(topic, bus, mediator))

}

object SubscribeActor {
  def props(topic: String, bus: ActorRef, mediator: ActorRef): Props =
    Props(new SubscribeActor(topic, bus, mediator))
}

class SubscribeActor(val topic: String, val bus: ActorRef, val mediator: ActorRef) extends Actor with ActorLogging {

  import context._

  mediator ! Subscribe(topic, self)

  override def receive: Receive = {
    case SubscribeAck(Subscribe(topic, self, _), fail) =>
      if (fail.isEmpty) become(ready)
      else log.error(fail.get, s"can't subscribe to $topic")
  }

  def ready: Receive = {
    case msg: Message =>
      log.info(s"Received msg : $msg")
      bus ! EventObject(msg.payload.toString)
  }
}

object MQTTService {
  def props(name: String, usePath: String): Props = Props(new MQTTService(name, usePath))
}

class MQTTService(val name: String, val usePath: String) extends Actor with ActorLogging {
  private val splitted = usePath.split("@@")
  private val broker = splitted(0)
  private val topic = splitted(1)

  private val mediator =
    context.actorOf(Props(classOf[MqttPubSub], PSConfig(brokerUrl = s"tcp://$broker", randomUUID().toString)))

  override def receive: Receive = {
    case EventObject(c) =>
      log.info(s"Receive message $c")
      mediator ! new Publish(topic, c.getBytes, 2)
    case EventJson(c) =>
      log.info(s"Receive message $c")
  }
}
