package cargo.engine

import scala.util.Try

case class SourceInstance(name: String, proto: String, path: String, method: Option[String])
case class ServiceInstance(name: String, proto: String, path: String, method: Option[String])
case class FlowInstance(from: SourceInstance, to: ServiceInstance)

class Semantics {

  @volatile var sources = Map[String, SourceInstance]()
  @volatile var services = Map[String, ServiceInstance]()
  @volatile var flows = List[FlowInstance]()

  def verify(rules: List[Rules]) = {
    Try {
      rules.foreach {
        case src: SourceObject =>
          val name = src.ident.content
          val exist = sources.exists(p => name == p._1)
          // TODO: custom an exception
          if (exist) throw new Exception("verification failed.")
          else {
            sources += (name -> SourceInstance(
              name,
              proto = src.states.get(Proto).map(_.content).get,
              path = src.states.get(Path).map(_.content).get,
              method = src.states.get(Method).map(_.content)))
          }
        case svc: ServiceObject =>
          val name = svc.ident.content
          val exist = services.exists(p => name == p._1)
          // TODO: custom an exception
          if (exist) throw new Exception("verification failed.")
          else {
            services += (name -> ServiceInstance(
              name,
              proto = svc.states.get(Proto).map(_.content).get,
              path = svc.states.get(Path).map(_.content).get,
              method = svc.states.get(Method).map(_.content)))
          }
        case flow: Flow =>
          // unwrap ident
          val (from, to) = (flow.from.content, flow.to.content)
          val src = sources.get(from).get
          val svc = services.get(to).get
          flows = FlowInstance(src, svc) :: flows
      }
    }
  }

}
