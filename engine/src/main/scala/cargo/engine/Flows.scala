package cargo.engine

import cargo.engine.compiler.ExecutionModel
import cargo.engine.proto.{HTTPSource, Protocol}

object Flows {
  def apply(model: ExecutionModel = ExecutionModel(Map(), Map(), List())): Flows = new Flows(model)
}

// Will be an singleton object.
class Flows(val model: ExecutionModel) {

  private val sources = Protocol.registerSources(model.sources)

  def route =
    sources.filter {
      case _: HTTPSource => true
      case _             => false
    } map {
      case hSource: HTTPSource => hSource.expose
    }

  def content = {
    s"""
       |sources:
       |  ${model.sources.keys.mkString("\n  ")}
       |
       |services:
       |  ${model.services.keys.mkString("\n  ")}
       |
       |flows:
       |  ${model.flows.map(f => s"${f.from.name} ~> ${f.to.name}").mkString("\n  ")}
     """.stripMargin
  }
}
