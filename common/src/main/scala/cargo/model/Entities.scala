package cargo.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Tag(tag: String)
case class Ident(tag: String, cls: String)
case class Cls(cls: String)
case class Shelf(cls: String, shelf: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val tagformat = jsonFormat1(Tag)
  implicit val identformat = jsonFormat2(Ident)
  implicit val clsformat = jsonFormat1(Cls)
  implicit val shelfformat = jsonFormat2(Shelf)
}
