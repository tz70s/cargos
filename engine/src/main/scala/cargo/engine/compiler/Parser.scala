package cargo.engine.compiler

import cargo.Logging

sealed trait Rules
sealed trait Definable
case class SourceObject(ident: Ident, states: Map[StateWord, Ident]) extends Definable with Rules
case class ServiceObject(ident: Ident, states: Map[StateWord, Ident]) extends Definable with Rules
case class Flow(from: Ident, to: Ident) extends Rules

class Parser extends Logging {

  // Matching rule:
  // program:
  //   definition
  //   | expression
  //
  // definition: DefineWord LeftBrace [statement]+ RightBrace
  //
  // DefineWord: source | service
  //
  // statement: (PROTO|METHOD|PATH) Ident
  //
  // expression: Ident FlowOp Ident

  // If stmt is empty and the head token is Ident: it is an expression.
  // Only one accepted expression => flowOp

  def parse(tokens: List[Token], rules: List[Rules] = List(), count: Int = 0): Option[List[Rules]] = {
    if (tokens.isEmpty) Some(rules)
    else {
      tokens.head match {
        case s: DefineWord =>
          tokens.tail.head match {
            case i: Ident =>
              dealDefine(i, s, tokens.tail.tail.tail, Map()) match {
                case (Some(r: Rules), newtokens: List[Token]) =>
                  parse(newtokens, r :: rules, count + tokens.size - newtokens.size)
                case _ => None
              }
            case _ => None
          }
        case from: Ident =>
          tokens.tail.head match {
            case FlowOp =>
              tokens.tail.tail.head match {
                case to: Ident => parse(tokens.tail.tail.tail, Flow(from, to) :: rules, count + 3)
                case _ =>
                  log.error(s"Match error at count : $count")
                  None
              }
            case _ =>
              log.error(s"Match error at count : $count")
              None
          }
        case _ =>
          log.error(s"Match error at count : $count")
          None
      }
    }
  }

  private def dealDefine(ident: Ident,
                         define: DefineWord,
                         tokens: List[Token],
                         stmt: Map[StateWord, Ident]): (Option[Definable], List[Token]) = {
    // Basically, we'll not reach this state
    if (tokens.isEmpty) (None, List())
    else {
      tokens.head match {
        case s: StateWord =>
          val next = tokens.tail.head
          next match {
            case i: Ident =>
              dealDefine(ident, define, tokens.tail.tail, stmt + (s -> i))
            case _ =>
              (None, List())
          }
        case RightBrace =>
          define match {
            case Source =>
              (Some(SourceObject(ident, stmt)), tokens.tail)
            case Service =>
              (Some(ServiceObject(ident, stmt)), tokens.tail)
          }
        case e =>
          log.error(s"Match error: $e")
          (None, List())
      }
    }
  }
}
