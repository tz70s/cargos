package cargo.engine.proto

import java.nio.charset.StandardCharsets
import java.util.UUID.randomUUID

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import cargo.engine.EventBus.EventJson
import com.sandinh.paho.akka._
import spray.json._
import DefaultJsonProtocol._
import cargo.Logging

class MQTTSource(val name: String, val userPath: String, val bus: ActorRef)(implicit val system: ActorSystem)
    extends ProtocolSource[Unit]
    with Logging {

  private val splitted = userPath.split("@@")
  private val broker = splitted(0)
  private val topic = splitted(1)

  override def expose = {}

  override def close(): Unit = {
    log.debug(s"close MQTT source : $name")
    mediator ! PoisonPill
    subscribeActor ! PoisonPill
  }

  private val mediator =
    system.actorOf(Props(classOf[MqttPubSub], PSConfig(brokerUrl = s"tcp://$broker", randomUUID().toString)))

  private val subscribeActor = system.actorOf(SubscribeActor.props(topic, bus, mediator))

}

object SubscribeActor {
  def props(topic: String, bus: ActorRef, mediator: ActorRef): Props =
    Props(new SubscribeActor(topic, bus, mediator))
}

class SubscribeActor(val topic: String, val bus: ActorRef, val mediator: ActorRef) extends Actor with Logging {

  import context._

  mediator ! Subscribe(topic, self)

  override def receive: Receive = {
    case SubscribeAck(Subscribe(topic, self, _), fail) =>
      if (fail.isEmpty) become(ready)
      else log.error(s"can't subscribe to $topic")
  }

  override def postStop(): Unit = {
    log.debug(s"close MQTT subscribe actor which subscribe to $topic.")
  }

  def ready: Receive = {
    case msg: Message =>
      val text = new String(msg.payload, StandardCharsets.UTF_8)
      val json = text.parseJson
      log.debug(s"received msg : ${json.compactPrint}")
      bus ! EventJson(json)
  }
}

object MQTTService {
  def props(name: String, usePath: String): Props = Props(new MQTTService(name, usePath))
}

class MQTTService(val name: String, val usePath: String) extends Actor with Logging {
  private val splitted = usePath.split("@@")
  private val broker = splitted(0)
  private val topic = splitted(1)

  override def postStop(): Unit = {
    log.debug(s"close MQTT service $name")
  }

  private val mediator =
    context.actorOf(Props(classOf[MqttPubSub], PSConfig(brokerUrl = s"tcp://$broker", randomUUID().toString)))

  override def receive: Receive = {
    case EventJson(c) =>
      log.debug(s"receive message ${c.compactPrint}")
      val msg = c.compactPrint.toCharArray.map(c => c.toByte)
      mediator ! new Publish(topic, msg, 2)
  }
}
