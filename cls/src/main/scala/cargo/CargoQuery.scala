package cargo

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import cargo.model.{Ident, JsonSupport, Tag}

import scala.util.{Failure, Success}

object CargoQuery {
  def apply()(implicit actorSystem: ActorSystem): CargoQuery = new CargoQuery()
}

class CargoQuery()(implicit actorSystem: ActorSystem) extends JsonSupport with Logging {

  private val store = CargoStore()
  private val transpost = Transpost(store)

  private val tag = path("tag")

  private val retrieve =
    (tag & post & entity(as[Tag])) { tag =>
      onComplete(transpost.transpost(tag)) {
        case Success(_) =>
          complete(tag.tag)
        case Failure(e) =>
          val text = s"Internal error from querying."
          log.error(s"$text ${e.getMessage}")
          complete(text)
      }
    }

  private val insert =
    (path("ident") & (post | put) & entity(as[Ident])) { ident =>
      onComplete(store.insert(ident)) {
        case Success(_) => complete(s"Insert a tag ${ident.tag} with class : ${ident.cls}")
        case Failure(e) =>
          log.error(s"Insert error: ${e.getMessage}")
          complete(s"Internal error on insertion.")
      }
    }

  private val list =
    (tag & get & onComplete(store.list())) {
      case Success(result) =>
        val text = if (result.nonEmpty) result.map(_.toString).reduce(_ + "\n" + _) else "empty"
        complete(text)
      case Failure(e) =>
        val text = s"Internal error while finding all docs."
        log.error(s"$text ${e.getMessage}")
        complete(text)
    }

  val route = retrieve ~ list ~ insert
}
