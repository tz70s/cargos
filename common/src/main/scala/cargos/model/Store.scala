package cargos.model

import cargos.CargosConfig
import cargos.model.CargoIdent.CargoIdent
import org.mongodb.scala.{MongoClient, MongoCollection}
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}

/** Convert case class into bson object. */
object CargoIdent {
  val codecRegistry = fromRegistries(fromProviders(classOf[CargoIdent]), DEFAULT_CODEC_REGISTRY)
  case class CargoIdent(_id: ObjectId, tagid: String, cargoClass: String)
  def apply(tagid: String, cargoClass: String) = CargoIdent(new ObjectId(), tagid, cargoClass)
}

object CargosStore {
  def apply(): CargosStore = new CargosStore()
}

class CargosStore {
  private[this] val config = CargosConfig.mongo
  private[this] val mongoClient = MongoClient(s"mongodb://${config.host}:${config.port}")

  private[this] val database = mongoClient.getDatabase("cargo")
  private[this] val cargoIdentCollection: MongoCollection[CargoIdent] = database.getCollection("cargo-ident")

  def getAllClassFromIdent() = {
    cargoIdentCollection.find().limit(100).toFuture()
  }
  def getClassFromIdent(tag: Tag) = {
    cargoIdentCollection.find(equal("tagid", tag.tag)).first().toFutureOption()
  }

  def insertCargoIdent(tagWithClass: TagWithClass) = {
    cargoIdentCollection.insertOne(CargoIdent(tagWithClass.tag, tagWithClass.classi)).toFutureOption()
  }
}
