package cargos

import akka.http.scaladsl.server.Directives._
import cargos.model.CargoIdent.CargoIdent
import cargos.model.{CargosStore, JsonSupport, Tag}

import scala.util.{Failure, Success}

object CargoQuery {
  def apply(): CargoQuery = new CargoQuery()
}

class CargoQuery extends JsonSupport with Logging {

  val store = CargosStore()

  val tag = path("tag")
  val route =
    (tag & post & entity(as[Tag])) { tag =>
      onComplete(store.getClassFromIdent(tag)) {
        case Success(result) =>
          complete(tag.tag)
        case Failure(ex) =>
          val text = s"Internal error from querying."
          log.error(s"Inernal error from querying ${ex.getMessage}")
          complete(text)
      }

    } ~
      (tag & get & onComplete(store.getAllClassFromIdent())) {
        case Success(result: Seq[CargoIdent]) =>
          val text = result.map(_.toString).reduce(_ + "\n" + _)
          complete(text)
        case Failure(ex) =>
          val text = s"Internal error from querying."
          log.error(s"Inernal error from querying ${ex.getMessage}")
          complete(text)
      }
}
