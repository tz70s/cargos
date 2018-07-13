package cargo.engine.proto

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.ByteString
import cargo.Logging
import cargo.engine.EventBus.{EventJson, EventObject}
import spray.json.JsValue

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

  override def expose =
    api {
      methodDirective {
        entity(as[JsValue]) { json =>
          bus ! EventJson(json)
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Hello, API $name here!"))
        }
      }
    }
}

object HTTPService {
  def props(name: String, usePath: String, method: String): Props = Props(new HTTPService(name, usePath, method))
}

class HTTPService(val name: String, val usePath: String, val method: String)
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

  private val http = Http()

  override def receive: Receive = {
    case EventObject(c) =>
      log.info(s"Send message $c")
      val msg = ByteString(c)
      http.singleRequest(HttpRequest(safeMethod, uri = uri, entity = msg)) foreach { h =>
        log.info(h.entity.dataBytes.toString())
      }
    case EventJson(c) =>
      log.info(s"Send $c")
      val entity = Marshal(c).to[RequestEntity]
      entity flatMap { e =>
        http.singleRequest(HttpRequest(safeMethod, uri = uri, entity = e))
      } foreach { h =>
        log.info(h.entity.dataBytes.toString())
      }
  }
}
