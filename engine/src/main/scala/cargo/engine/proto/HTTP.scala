package cargo.engine.proto

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import cargo.Logging
import cargo.engine.EventBus.EventJson
import spray.json._

/** Currently, http source will drop out the path, since it's not that necessary */
class HTTPSource(val name: String, val usePath: String, val method: String, val bus: ActorRef)
    extends ProtocolSource[Route]
    with Logging
    with SprayJsonSupport {

  private val api = path(usePath.substring(1))

  private val methodDirective = method match {
    case "GET"    => get
    case "POST"   => post
    case "PUT"    => put
    case "DELETE" => delete
  }

  override def close(): Unit = {
    log.debug(s"close http source: $name")
  }

  override def expose =
    api {
      methodDirective {
        entity(as[JsValue]) { json =>
          bus ! EventJson(json)
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Source $name with payload: ${json.toString()}"))
        }
      }
    }
}

object HTTPService {
  def props(name: String, usePath: String, method: String)(implicit materializer: Materializer): Props =
    Props(new HTTPService(name, usePath, method))
}

class HTTPService(val name: String, val usePath: String, val method: String)(implicit val materializer: Materializer)
    extends Actor
    with ActorLogging
    with SprayJsonSupport {

  implicit val system = context.system
  implicit val ec = context.dispatcher

  private val uri = s"http://$usePath"

  private val safeMethod = method match {
    case "GET"    => HttpMethods.GET
    case "POST"   => HttpMethods.POST
    case "PUT"    => HttpMethods.PUT
    case "DELETE" => HttpMethods.DELETE
  }

  override def postStop(): Unit = {
    log.debug(s"close HTTP service $name")
  }

  private val http = Http()

  override def receive: Receive = {
    case EventJson(c) =>
      val entity = Marshal(c).to[RequestEntity]
      entity flatMap { e =>
        http.singleRequest(HttpRequest(safeMethod, uri = uri, entity = e))
      } flatMap { h =>
        h.entity.dataBytes.runForeach { bytes =>
          val text = bytes.mkString("")
          log.debug(s"HTTP source receive $text as response")
        }
      }
  }
}
