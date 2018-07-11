package cargos

import akka.event.slf4j.Logger

trait Logging {
  implicit val log = Logger(this.getClass.getName)
}
