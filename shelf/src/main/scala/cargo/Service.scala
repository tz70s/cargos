package cargo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import cargo.Generals._

object Service extends Logging {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("cargo-system")
    implicit val materializer = ActorMaterializer()

    val config = CargoConfig.shelf
    log.info(s"Spawn cargo-shelf service at http://${config.host}")

    val routes = info ~ apis(apiInfo ~ CargoQuery().route) ~ health

    Http().bindAndHandle(routes, "0.0.0.0", 8181)
  }
}
