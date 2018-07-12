package cargo.engine

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cargo.{CargoConfig, Logging}
import cargo.Generals.info
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

object Engine extends Logging {

  val testDataFlow =
    """
      |source TagSource {
      |  proto mqtt
      |  path tag
      |}
      |
      |service ClsService {
      |  proto http
      |  path /api/tag
      |  method POST
      |}
      |
      |TagSource ~> ClsService 
    """.stripMargin

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("cargo-system")
    implicit val materializer = ActorMaterializer()

    log.info(s"Spawn an engine service at http://localhost:8080")

    val routes = info ~ Deploy().deploy

    Http().bindAndHandle(routes, "0.0.0.0", 8080)
  }
}
