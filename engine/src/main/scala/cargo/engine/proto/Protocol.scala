package cargo.engine.proto

import cargo.engine.compiler.SourceInstance

class Protocol {}

trait ProtocolSource[U] {
  def expose: U
}

object Protocol {

  def registerSources(sources: Map[String, SourceInstance]) = {
    sources.map {
      case (_, src) =>
        sourceMatcher(src)
    }
  }

  def sourceMatcher(source: SourceInstance) = source.proto match {
    case "HTTP" => new HTTPSource(name = source.name, userPath = source.path, method = source.method.getOrElse("GET"))
    case "MQTT" => new MQTTSource(name = source.name, userPath = source.path)
    case _      => throw new Exception("unsupported protocol on sources.")
  }
}
