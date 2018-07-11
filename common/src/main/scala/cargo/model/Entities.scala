package cargo.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Tag(tag: String)
case class Ident(tag: String, cls: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val tagformat = jsonFormat1(Tag)
  implicit val identformat = jsonFormat2(Ident)
}
