package cargo.engine.compiler

import scala.util.Try

case class SourceInstance(name: String, proto: String, path: String, method: Option[String])
case class SinkInstance(name: String, proto: String, path: String, method: Option[String])
case class FlowInstance(from: SourceInstance, to: SinkInstance)
case class FlowOneToMany(from: SourceInstance, to: List[SinkInstance])

object Semantics {
  def apply(): Semantics = new Semantics()
}

class Semantics {

  private var _sources = Map[String, SourceInstance]()
  private var _sinks = Map[String, SinkInstance]()
  private var _flows = List[FlowInstance]()

  def flows = _flows

  def defVerify(rules: List[Rules]): Try[List[Rules]] = {
    Try {
      rules.map {
        case src: SourceObject =>
          val name = src.ident.content
          val exist = _sources.exists(p => name == p._1)
          // TODO: custom an exception
          if (exist) throw new Exception("verification failed.")
          else {
            _sources += (name -> SourceInstance(
              name,
              proto = src.states(StateDesc("proto")).content,
              path = src.states(StateDesc("path")).content,
              method = src.states.get(StateDesc("method")).map(_.content)))
            src
          }
        case svc: SinkObject =>
          val name = svc.ident.content
          val exist = _sinks.exists(p => name == p._1)
          // TODO: custom an exception
          if (exist) throw new Exception("verification failed.")
          else {
            _sinks += (name -> SinkInstance(
              name,
              proto = svc.states(StateDesc("proto")).content,
              path = svc.states(StateDesc("path")).content,
              method = svc.states.get(StateDesc("method")).map(_.content)))
            svc
          }
        case remain => remain
      }
    }
  }

  def flowVerify(rules: List[Rules]): Try[Unit] = {
    Try {
      rules foreach {
        case flow: Flow =>
          // unwrap ident
          val (from, to) = (flow.from.content, flow.to.content)
          val src = _sources(from)
          val svc = _sinks(to)
          _flows = FlowInstance(src, svc) :: _flows
        case _ => // ignore
      }
    }
  }

  def mergeFlow(rules: List[FlowInstance]): List[FlowOneToMany] = {
    val merged = rules.groupBy(f => f.from).map {
      case (src, list) =>
        FlowOneToMany(src, list.map(_.to))
    }
    merged.toList
  }

}
