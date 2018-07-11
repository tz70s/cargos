package cargo

import akka.http.scaladsl.server.Directives._

object CargoQuery {
  def apply(): CargoQuery = new CargoQuery()
}

class CargoQuery {

  val route =
    path("tag" / Segment) { tagid =>
      (get | put) {
        complete(s"Received put tagid : $tagid")
      }
    }
}
