package cargos

import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import cargos.Generals._

object Service {

  private val log = Logger("service-main")

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("cargos-system")
    implicit val materializer = ActorMaterializer()

    val config = CargosConfig.classi
    log.info(s"Spawn cargo service at http://${config.host}:${config.port}")

    val routes = info ~ apis(apiInfo ~ CargoQuery().route) ~ health

    Http().bindAndHandle(routes, "0.0.0.0", config.port)
  }
}
