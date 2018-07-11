package cargos

import pureconfig.loadConfigOrThrow

case class ClassiServiceConfig(host: String, port: Int)
case class ArmServiceConfig(host: String, port: Int)
case class ShelfServiceConfig(host: String, port: Int)
case class MongoConfig(host: String, port: Int)

object CargosConfig {
  val classi = loadConfigOrThrow[ClassiServiceConfig]("cargos.service.classi")
  val arm = loadConfigOrThrow[ArmServiceConfig]("cargos.service.arm")
  val shelf = loadConfigOrThrow[ShelfServiceConfig]("cargos.service.shelf")
  val mongo = loadConfigOrThrow[MongoConfig]("cargos.mongo")
}
