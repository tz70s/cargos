package cargo.engine.compiler

import scala.util.Try

case class SourceInstance(name: String, proto: String, path: String, method: Option[String])
case class ServiceInstance(name: String, proto: String, path: String, method: Option[String])
case class FlowInstance(from: SourceInstance, to: ServiceInstance)
case class FlowOneToMany(from: SourceInstance, to: List[ServiceInstance])

object Semantics {
  def apply(): Semantics = new Semantics()
}

class Semantics {

  private var _sources = Map[String, SourceInstance]()
  private var _services = Map[String, ServiceInstance]()
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
              proto = src.states(Proto).content,
              path = src.states(Path).content,
              method = src.states.get(Method).map(_.content)))
            src
          }
        case svc: ServiceObject =>
          val name = svc.ident.content
          val exist = _services.exists(p => name == p._1)
          // TODO: custom an exception
          if (exist) throw new Exception("verification failed.")
          else {
            _services += (name -> ServiceInstance(
              name,
              proto = svc.states(Proto).content,
              path = svc.states(Path).content,
              method = svc.states.get(Method).map(_.content)))
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
          val svc = _services(to)
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
