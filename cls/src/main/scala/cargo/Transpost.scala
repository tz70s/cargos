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

class Transpost(private val store: CargoStore)(implicit actorSystem: ActorSystem)
    extends SprayJsonSupport
    with Logging {

  private implicit val ec = actorSystem.dispatcher

  private val armIdent = s"http://${CargoConfig.arm.host}/classify"

  private val http = Http()

  def transpost(tag: Tag) = {
    val retrieve = store.retrieve(tag)
    retrieve flatMap { doc =>
      val cls = doc.get("cls").map(_.asString()).map(_.getValue)
      cls match {
        case Some(v) =>
          log.info(s"Received cls : $cls")
          val json = JsObject("cls" -> JsString(v))
          Marshal(json).to[RequestEntity]
        case None =>
          log.info("not found")
          throw new Exception("the query result is not existed.")
      }
    } flatMap { entity =>
      val armRequest = HttpRequest(uri = armIdent, method = HttpMethods.POST, entity = entity)
      http.singleRequest(armRequest)
    } flatMap { armr =>
      if (armr.status.isSuccess()) Future.successful(())
      else Future.failed(throw new Exception("error occurred on transpost to arm and shelf services"))
    }
  }
}
