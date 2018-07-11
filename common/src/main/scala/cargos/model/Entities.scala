package cargos.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Tag(tag: String)
case class TagWithClass(tag: String, classi: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val tagformat = jsonFormat1(Tag)
}
