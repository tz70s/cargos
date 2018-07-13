package cargo

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, path, pathEndOrSingleSlash, pathPrefix}

/** An unified place for providing general routes and hierarchy. */
object Generals {

  /** wrapper with single api prefix */
  private[cargo] val apis = pathPrefix("api")

  private[cargo] val apiInfo = pathEndOrSingleSlash {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Hello, APIs here!"))
  }

  private[cargo] val info = path("") {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Hello world!"))
  }

  private[cargo] val health = apis {
    path("ping") {
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "pong!"))
    }
  }
}
