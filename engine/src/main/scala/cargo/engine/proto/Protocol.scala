package cargo.engine.proto

import akka.actor.ActorSystem
import cargo.Logging
import cargo.engine.EventBus
import cargo.engine.compiler.FlowOneToMany

trait ProtocolSource[U] {
  def expose: U
}

trait ProtocolService

class ProtocolBindings(val flow: FlowOneToMany)(implicit val system: ActorSystem) extends Logging {

  val services = flow.to.map { svc =>
    svc.proto match {
      // TODO: Remove get or else
      case "HTTP" => system.actorOf(HTTPService.props(svc.name, svc.path, svc.method.getOrElse("GET")))
      case "MQTT" => system.actorOf(MQTTService.props(svc.name, svc.path))
    }
  }

  val eventBus = system.actorOf(EventBus.props(flow.toString, services))

  val source = flow.from.proto match {
    case "HTTP" =>
      // TODO: Remove get or else
      new HTTPSource(
        name = flow.from.name,
        usePath = flow.from.path,
        method = flow.from.method.getOrElse("GET"),
        bus = eventBus)
    case "MQTT" =>
      new MQTTSource(name = flow.from.name, userPath = flow.from.path, bus = eventBus)
  }

  log.info(s"Construct with $services, $eventBus, $source")
}
