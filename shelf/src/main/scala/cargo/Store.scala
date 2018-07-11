package cargo

import cargo.model._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters._

object CargoStore {
  def apply(): CargoStore = new CargoStore()
}

class CargoStore extends StoreAPI {

  private[this] val collect = database.getCollection("cargo-shelf")

  def list() = {
    collect.find().toFuture()
  }

  def retrieve(cls: Cls) = {
    collect.find(equal("cls", cls.cls)).first().toFuture()
  }

  def insert(shelf: Shelf) = {
    val doc = Document("cls" -> shelf.cls, "shelf" -> shelf.shelf)
    collect.insertOne(doc).toFuture()
  }
}
