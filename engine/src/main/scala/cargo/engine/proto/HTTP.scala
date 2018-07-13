package cargo.engine.proto

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cargo.Logging

/** Currently, http source will drop out the path, since it's not that necessary */
class HTTPSource(val name: String, val userPath: String, val method: String)
    extends ProtocolSource[Route]
    with Logging {
  private val api = path(userPath.substring(1))

  private val methodDirective = method match {
    case "GET"    => get
    case "POST"   => post
    case "PUT"    => put
    case "DELETE" => delete
  }

  override def expose =
    api {
      methodDirective {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Hello, API $name here!"))
      }
    }
}
