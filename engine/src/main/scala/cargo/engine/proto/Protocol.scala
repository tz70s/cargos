package cargo.engine.proto

import akka.actor.{ActorSystem, PoisonPill}
import akka.stream.Materializer
import cargo.Logging
import cargo.engine.EventBus
import cargo.engine.compiler.FlowOneToMany

trait ProtocolSource[U] {
  def expose: U
  def close()
}

class ProtocolBindings(val flow: FlowOneToMany)(implicit val system: ActorSystem,
                                                implicit val materializer: Materializer)
    extends Logging {

  val sinks = flow.to.map { sink =>
    sink.proto match {
      // TODO: Remove get or else
      case "http" => system.actorOf(HTTPSink.props(sink.name, sink.path, sink.method.getOrElse("get")))
      case "mqtt" => system.actorOf(MQTTSink.props(sink.name, sink.path))
    }
  }

  val eventBus = system.actorOf(EventBus.props(flow.toString, sinks))

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
