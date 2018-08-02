package cargo.engine.proto

import java.nio.charset.StandardCharsets

import akka.actor.{Actor, ActorRef, Props}
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
    case "get"    => get
    case "post"   => post
    case "put"    => put
    case "delete" => delete
  }

  override def close(): Unit = {
    log.debug(s"close http source: $name")
  }

  override def expose =
    api {
      methodDirective {
        entity(as[JsValue]) { json =>
          log.debug(s"receive payload from http source : ${json.compactPrint}")
          bus ! EventJson(json)
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Source $name with payload: ${json.toString()}"))
        }
      }
    }
}

object HTTPSink {
  def props(name: String, usePath: String, method: String)(implicit materializer: Materializer): Props =
    Props(new HTTPSink(name, usePath, method))
}

class HTTPSink(val name: String, val usePath: String, val method: String)(implicit val materializer: Materializer)
    extends Actor
    with Logging
    with SprayJsonSupport {

  implicit val system = context.system
  implicit val ec = context.dispatcher

  private val uri = s"http://$usePath"

  private val safeMethod = method match {
    case "get"    => HttpMethods.GET
    case "post"   => HttpMethods.POST
    case "put"    => HttpMethods.PUT
    case "delete" => HttpMethods.DELETE
  }

  override def postStop(): Unit = {
    log.debug(s"close http sink $name")
  }

  private val http = Http()

  override def receive: Receive = {
    case EventJson(c) =>
      log.debug(s"preparing to send via http : ${c.compactPrint}")
      val entity = Marshal(c).to[RequestEntity]
      entity flatMap { e =>
        http.singleRequest(HttpRequest(safeMethod, uri = uri, entity = e))
      } flatMap { h =>
        h.entity.dataBytes.runForeach { bytes =>
          val text = bytes.decodeString(StandardCharsets.UTF_8)
          log.debug(s"http source receive $text as response")
        }
      }
  }
}
