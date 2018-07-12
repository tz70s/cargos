package cargo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RequestEntity}
import cargo.model.Tag
import spray.json.{JsObject, JsString}

import scala.concurrent.Future

object Transpost {
  def apply(store: CargoStore)(implicit actorSystem: ActorSystem): Transpost = new Transpost(store)
}

class Transpost(private val store: CargoStore)(implicit actorSystem: ActorSystem) extends SprayJsonSupport {

  private implicit val ec = actorSystem.dispatcher

  private val armIdent = s"http://${CargoConfig.arm.host}:${CargoConfig.arm.port}/api/classify"
  private val shelfIdent = s"http://${CargoConfig.shelf.host}:${CargoConfig.shelf.port}/api/cls"

  private val http = Http()

  def transpost(tag: Tag) = {
    val retrieve = store.retrieve(tag)
    retrieve flatMap { doc =>
      val cls = doc.get("cls").map(_.asString()).map(_.getValue)
      cls match {
        case Some(v) =>
          val json = JsObject("cls" -> JsString(v))
          Marshal(json).to[RequestEntity]
        case None =>
          throw new Exception("the query result is not existed.")
      }
    } flatMap { entity =>
      val armRequest = HttpRequest(uri = armIdent, method = HttpMethods.POST, entity = entity)
      val shelfRequest = HttpRequest(uri = shelfIdent, method = HttpMethods.POST, entity = entity)
      for {
        armr <- http.singleRequest(armRequest)
        shelfr <- http.singleRequest(shelfRequest)
      } yield (armr, shelfr)
    } flatMap {
      case (armr, shelfr) =>
        if (armr.status.isSuccess() && shelfr.status.isSuccess()) Future.successful(())
        else Future.failed(throw new Exception("error occurred on transpost to arm and shelf services"))
    }
  }
}
