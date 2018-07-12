package cargo.engine

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import cargo.Logging
import spray.json.DefaultJsonProtocol

case class Script(source: String)

trait ScriptJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val tagformat = jsonFormat1(Script)
}

object Deploy {
  def apply(): Deploy = new Deploy()
}

class Deploy extends ScriptJsonSupport with Logging {
  private val lexer = new Lexer()
  private val parser = new Parser()

  private def compile(source: String) = {
    log.info(source)
    val tokens = lexer.lex(List(), source.toList, List())
    log.info(s"Token Stream: ${tokens.reverse.mkString(" ")}")
    val rules = parser.parse(tokens.reverse, List(), 0)
    rules match {
      case Some(r) =>
        log.info(r.mkString(" "))
        Some(r)
      case None =>
        log.error(s"Compile error ...")
        None
    }
  }

  val deploy =
    (path("deploy") & entity(as[Script])) { script =>
      val rules = compile(script.source)
      val text = rules match {
        case Some(r) => r.reverse.mkString("\n")
        case None    => "Seems there's an compile error, please check the source ..."
      }
      complete(text)
    }
}
