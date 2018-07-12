package cargo.model

import cargo.CargoConfig
import org.mongodb.scala.MongoClient

object Store {
  private[model] val config = CargoConfig.mongo
}

trait StoreAPI {
  import Store._

  private[this] val mongoClient = MongoClient(
    s"mongodb://${config.user}:${config.password}@${config.host}/?authSource=cargo")

  protected[cargo] val database = mongoClient.getDatabase("cargo")
}
