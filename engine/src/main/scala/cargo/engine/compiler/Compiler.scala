package cargo.engine.compiler

import cargo.Logging

import scala.util.Failure

case class ExecutionModel(sources: Map[String, SourceInstance],
                          services: Map[String, ServiceInstance],
                          flows: List[FlowInstance])

object Compiler extends Logging {

  private val lexer = new Lexer()
  private val parser = new Parser()

  def compile(source: String) = {
    val tokens = lexer.lex(source.toList)
    log.debug(s"Token Stream: ${tokens.reverse.mkString(" ")}")
    val rules = parser.parse(tokens.reverse)
    rules match {
      case Some(r) =>
        val rev = r.reverse
        log.debug(s"Compiled objects : ${rev.mkString(" ")}")
        val semantics = Semantics()
        semantics.defVerify(rev) flatMap { afterrev =>
          semantics.flowVerify(afterrev).map { _ =>
            ExecutionModel(semantics.sources, semantics.services, semantics.flows)
          }
        }
      case None =>
        log.error(s"Compile error ...")
        Failure(new Exception("Parsing exception ..."))
    }
  }
}
