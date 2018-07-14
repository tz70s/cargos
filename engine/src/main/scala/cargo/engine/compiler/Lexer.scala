package cargo.engine.compiler

import scala.annotation.tailrec

trait DefineWord
trait StateWord
sealed trait Token
case object Sink extends Token with DefineWord
case object Source extends Token with DefineWord
case object FlowOp extends Token
case object LeftBrace extends Token
case object RightBrace extends Token
case class Ident(content: String) extends Token
case class StateDesc(content: String) extends Token

object Tokens {
  val stateDesc = List("proto", "path", "method")
}

class Lexer {

  def lex(data: List[Char], tokens: List[Token] = List(), buffer: List[Char] = List()): List[Token] = {
    if (data.isEmpty) tokens
    else {
      data.head match {
        case ' ' | '\n' | '\r' =>
          if (buffer.isEmpty) lex(data.tail, tokens)
          else {
            val token = keywordsOrIdent(buffer)
            lex(data.tail, token :: tokens)
          }
        case '{' =>
          if (buffer.isEmpty) lex(data.tail, LeftBrace :: tokens)
          else {
            val token = keywordsOrIdent(buffer)
            lex(data.tail, LeftBrace :: token :: tokens)
          }
        case '}' =>
          if (buffer.isEmpty) lex(data.tail, RightBrace :: tokens)
          else {
            val token = keywordsOrIdent(buffer)
            lex(data.tail, RightBrace :: token :: tokens)
          }
        case '#' =>
          // flush out until meet newline
          if (buffer.isEmpty) lex(comment(data.tail), tokens, buffer)
          else {
            val token = keywordsOrIdent(buffer)
            lex(data.tail, token :: tokens)
          }
        case c =>
          lex(data.tail, tokens, c :: buffer)
      }
    }
  }

  private def keywordsOrIdent(buffer: List[Char]): Token = {
    buffer.reverse.mkString("") match {
      case "sink"                              => Sink
      case "source"                            => Source
      case "~>"                                => FlowOp
      case sc if Tokens.stateDesc.contains(sc) => StateDesc(sc)
      case e                                   => Ident(e)
    }
  }

  @tailrec
  private def comment(data: List[Char]): List[Char] = {
    if ((data.head == '\r') || (data.head == '\n')) data.tail
    else comment(data.tail)
  }
}
