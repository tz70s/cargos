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

  val keywords = List("service", "source", "proto", "path", "method")

  val flowOp = "~>"
  val whitespace = ' '

  def lex(tokens: List[Token], data: List[Char], buffer: List[Char]): List[Token] = {
    if (data.isEmpty) tokens
    else {
      data.head match {
        case ' ' | '\n' =>
          if (buffer.isEmpty) lex(tokens, data.tail, List())
          else {
            val token = keywordsOrIdent(buffer)
            lex(token :: tokens, data.tail, List())
          }
        case '{' =>
          if (buffer.isEmpty) lex(LeftBrace :: tokens, data.tail, List())
          else {
            val token = keywordsOrIdent(buffer)
            lex(LeftBrace :: token :: tokens, data.tail, List())
          }
        case '}' =>
          if (buffer.isEmpty) lex(RightBrace :: tokens, data.tail, List())
          else {
            val token = keywordsOrIdent(buffer)
            lex(RightBrace :: token :: tokens, data.tail, List())
          }
        case c =>
          lex(tokens, data.tail, c :: buffer)
      }
    }
  }

  def keywordsOrIdent(buffer: List[Char]): Token = {
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
