package cargo

import cargo.model.{Ident, StoreAPI, Tag}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters._

object CargoStore {
  def apply(): CargoStore = new CargoStore()
}

class CargoStore extends StoreAPI {

  private[this] val collect = database.getCollection("cargo-ident")

  def list() = {
    collect.find().toFuture()
  }

  def retrieve(tag: Tag) = {
    collect.find(equal("tag", tag.tag)).first().toFuture()
  }

  def insert(ident: Ident) = {
    val doc = Document("tag" -> ident.tag, "class" -> ident.cls)
    collect.insertOne(doc).toFuture()
  }
}
