package cargos

import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

object Service {

  private val log = Logger("service-main")

  /** wrapper with single api prefix */
  private val apis = pathPrefix("api")

  private val info = pathEndOrSingleSlash {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Hello, APIs here!"))
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("cargos-system")
    implicit val materializer = ActorMaterializer()

    val port = 8080
    log.info(s"Spawn cargo service at port $port")

    val routes = apis(info ~ CargoQuery().route)

    Http().bindAndHandle(routes, "0.0.0.0", port)
  }
}
