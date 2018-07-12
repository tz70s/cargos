package cargo

import pureconfig.loadConfigOrThrow

case class ServiceConfig(host: String, port: Int)
case class MongoConfig(user: String, password: String, host: String, port: Int)

object CargoConfig {
  val cls = loadConfigOrThrow[ServiceConfig]("cargo.service.cls")
  val arm = loadConfigOrThrow[ServiceConfig]("cargo.service.arm")
  val shelf = loadConfigOrThrow[ServiceConfig]("cargo.service.shelf")
  val autonomous = loadConfigOrThrow[ServiceConfig]("cargo.service.autonomous")
  val mongo = loadConfigOrThrow[MongoConfig]("cargo.mongo")
}
