package cargo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RequestEntity}
import cargo.model.Cls
import spray.json.{DefaultJsonProtocol, JsObject, JsString}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import scala.concurrent.Future

object Transpost {
  def apply(store: CargoStore)(implicit actorSystem: ActorSystem): Transpost = new Transpost(store)
}

class Transpost(private val store: CargoStore)(implicit actorSystem: ActorSystem) extends SprayJsonSupport {

  implicit val ec = actorSystem.dispatcher

  private val autonomousIdent =
    s"http://${CargoConfig.autonomous.host}:${CargoConfig.autonomous.port}/api/transportation"

  private val http = Http()

  def transpost(cls: Cls) = {
    val retrieve = store.retrieve(cls)
    retrieve flatMap { doc =>
      val shelf = doc.get("shelf").map(_.asString()).map(_.getValue)
      shelf match {
        case Some(v) =>
          val json = JsObject("shelf" -> JsString(v))
          Marshal(json).to[RequestEntity]
        case None =>
          throw new Exception("the query result is not existed.")
      }
    } flatMap { entity =>
      val autoRequest = HttpRequest(uri = autonomousIdent, method = HttpMethods.POST, entity = entity)
      http.singleRequest(autoRequest)
    } flatMap { resp =>
      if (resp.status.isSuccess()) Future.successful(())
      else Future.failed(throw new Exception("error occurred on transpost to autonomous car service"))
    }
  }
}
