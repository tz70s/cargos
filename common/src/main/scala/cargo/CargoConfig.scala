package cargo

import pureconfig.loadConfigOrThrow

case class ClsServiceConfig(host: String, port: Int)
case class ArmServiceConfig(host: String, port: Int)
case class ShelfServiceConfig(host: String, port: Int)
case class MongoConfig(host: String, port: Int)

object CargoConfig {
  val cls = loadConfigOrThrow[ClsServiceConfig]("cargo.service.cls")
  val arm = loadConfigOrThrow[ArmServiceConfig]("cargo.service.arm")
  val shelf = loadConfigOrThrow[ShelfServiceConfig]("cargo.service.shelf")
  val mongo = loadConfigOrThrow[MongoConfig]("cargo.mongo")
}
