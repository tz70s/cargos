package cargo.engine

trait DefineWord
trait StateWord
sealed trait Token
case object Service extends Token with DefineWord
case object Source extends Token with DefineWord
case object Proto extends Token with StateWord
case object Path extends Token with StateWord
case object Method extends Token with StateWord
case object FlowOp extends Token
case object LeftBrace extends Token
case object RightBrace extends Token
case class Ident(content: String) extends Token

class Lexer {

  def lex(data: List[Char], tokens: List[Token] = List(), buffer: List[Char] = List()): List[Token] = {
    if (data.isEmpty) tokens
    else {
      data.head match {
        case ' ' | '\n' =>
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
        case c =>
          lex(data.tail, tokens, c :: buffer)
      }
    }
  }

  private def keywordsOrIdent(buffer: List[Char]): Token = {
    buffer.reverse.mkString("") match {
      case "service" => Service
      case "source"  => Source
      case "proto"   => Proto
      case "method"  => Method
      case "path"    => Path
      case "~>"      => FlowOp
      case e         => Ident(e)
    }
  }
}
