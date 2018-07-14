package cargo.engine

import akka.actor.ActorSystem
import akka.stream.Materializer
import cargo.engine.compiler.ExecutionModel
import cargo.engine.proto.{HTTPSource, ProtocolBindings}

object Flows {
  def apply(model: ExecutionModel = ExecutionModel(List()))(implicit system: ActorSystem,
                                                            materializer: Materializer): Flows = new Flows(model)
}

// Will be an singleton object.
class Flows(val model: ExecutionModel)(implicit val system: ActorSystem, val materializer: Materializer) {

  private val actorFlows = model.flows.map { flow =>
    new ProtocolBindings(flow)
  }

  def cleanup = actorFlows.foreach(_.cleanup)

  def route =
    actorFlows.map(_.source).filter {
      case _: HTTPSource => true
      case _             => false
    } map {
      case hSource: HTTPSource => hSource.expose
    }

  def content = {
    s"""
       |flows:
       |  ${model.flows.map(f => s"${f.from.name} ~> [${f.to.map(_.name).mkString(" ")}]").mkString("\n  ")}
     """.stripMargin
  }
}
