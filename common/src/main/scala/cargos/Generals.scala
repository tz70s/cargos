package cargos

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, path, pathEndOrSingleSlash, pathPrefix}

/** An unified place for providing general routes and hierarchy. */
object Generals {

  /** wrapper with single api prefix */
  private[cargos] val apis = pathPrefix("api")

  private[cargos] val apiInfo = pathEndOrSingleSlash {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Hello, APIs here!"))
  }

  private[cargos] val info = pathEndOrSingleSlash {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Hello world!"))
  }

  private[cargos] val health = apis {
    path("ping") {
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "pong!"))
    }
  }
}
