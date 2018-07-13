package cargo.engine

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cargo.Logging
import spray.json.DefaultJsonProtocol

import scala.util.{Failure, Success}

case class Script(source: String)

trait ScriptJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val tagformat = jsonFormat1(Script)
}

object Deploy {
  def apply(): Deploy = new Deploy()
}

class Deploy extends ScriptJsonSupport with Logging {

  @volatile private var flows = Flows()

  @volatile private var sources = List.empty[Route]

  private val deploy: Route =
    (path("deploy") & entity(as[Script])) { script =>
      val model = compiler.Compiler.compile(script.source)
      val text = model match {
        case Success(em) =>
          flows = Flows(em)
          log.info(flows.model.sources.toString())
          sources = flows.route.toList
          log.info(sources.toString())
          flows.content
        case Failure(_) => "Seems there's an compile error, please check the source ..."
      }
      complete(text)
    }

  private val dynRoute: Route = ctx => concat(sources: _*)(ctx)

  val route = deploy ~ dynRoute
}
