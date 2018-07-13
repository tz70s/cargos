package cargo.engine.proto

class MQTTSource(val name: String, val userPath: String) extends ProtocolSource[Unit] {

  override def expose = {}
}
