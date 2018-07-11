package cargo

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import cargo.model._

import scala.util.{Failure, Success}

object CargoQuery {
  def apply()(implicit actorSystem: ActorSystem): CargoQuery = new CargoQuery()
}

class CargoQuery(implicit actorSystem: ActorSystem) extends JsonSupport with Logging {

  private val store = CargoStore()
  private val transpost = Transpost(store)

  private val cls = path("cls")

  private val retrieve =
    (cls & post & entity(as[Cls])) { c =>
      onComplete(transpost.transpost(c)) {
        case Success(_) =>
          complete(c.cls)
        case Failure(e) =>
          val text = s"Internal error from querying."
          log.error(s"$text ${e.getMessage}")
          complete(text)
      }
    }

  private val insert =
    (path("shelf") & (post | put) & entity(as[Shelf])) { shelf =>
      onComplete(store.insert(shelf)) {
        case Success(_) => complete(s"Insert a shelf ${shelf.shelf} with class : ${shelf.cls}")
        case Failure(e) =>
          log.error(s"Insert error: ${e.getMessage}")
          complete(s"Internal error on insertion.")
      }
    }

  private val list =
    (cls & get & onComplete(store.list())) {
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
