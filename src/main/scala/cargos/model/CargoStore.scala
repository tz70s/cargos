package cargos.model

import cargos.model.CargoIdent.CargoIdent
import org.mongodb.scala.{MongoClient, MongoCollection}
import pureconfig.loadConfigOrThrow
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

case class MongoConfig(host: String, port: Int)

object CargoStore {
  def apply(): CargoStore = new CargoStore()
}

class CargoStore {
  private[this] val config = loadConfigOrThrow[MongoConfig]("mongo")
  private[this] val mongoClient = MongoClient(s"mongodb://${config.host}:${config.port}")

  private val database = mongoClient.getDatabase("cargo")
  private val cargoIdentCollection: MongoCollection[CargoIdent] = database.getCollection("cargo-ident")

  def getClassFromIdent(tagid: String) = {
    cargoIdentCollection.find(equal("tagid", tagid)).first().toFutureOption()
  }

  def insertCargoIdent(tagid: String, cargoClass: String) = {
    cargoIdentCollection.insertOne(CargoIdent(tagid, cargoClass)).toFutureOption()
  }
}
