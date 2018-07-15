package cargo.engine.proto

import akka.actor.{ActorSystem, PoisonPill}
import akka.stream.Materializer
import cargo.Logging
import cargo.engine.EventBus
import cargo.engine.compiler.FlowOneToMany
import java.util.UUID.randomUUID

trait ProtocolSource[U] {
  def expose: U
  def close()
}

class ProtocolBindings(val flow: FlowOneToMany)(implicit val system: ActorSystem,
                                                implicit val materializer: Materializer)
    extends Logging {

  val traceid = randomUUID()

  val sinks = flow.to.map { sink =>
    sink.proto match {
      // TODO: Remove get or else
      case "http" =>
        system.actorOf(
          HTTPSink.props(sink.name, sink.path, sink.method.getOrElse("get")),
          s"sink-${sink.name}-$traceid")
      case "mqtt" => system.actorOf(MQTTSink.props(sink.name, sink.path), s"sink-${sink.name}-$traceid")
    }
  }

  val eventBus =
    system.actorOf(
      EventBus.props(s"event-bus-${flow.from.name}-${flow.to.mkString(", ")}-$traceid", flow.from.name, sinks))

  val source = flow.from.proto match {
    case "http" =>
      // TODO: Remove get or else
      new HTTPSource(
        name = flow.from.name,
        usePath = flow.from.path,
        method = flow.from.method.getOrElse("get"),
        bus = eventBus)
    case "mqtt" =>
      new MQTTSource(name = flow.from.name, usePath = flow.from.path, bus = eventBus)
  }

  def cleanup = {
    eventBus ! PoisonPill
    sinks.foreach(_ ! PoisonPill)
    source.close()
  }
}
