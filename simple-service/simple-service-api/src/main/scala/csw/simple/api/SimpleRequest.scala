package csw.simple.api

sealed trait SimpleRequest

object SimpleRequest {
  case class Hello(name: String)          extends SimpleRequest
  case class HelloStream(name: String)    extends SimpleRequest
  case class Square(number: Int)          extends SimpleRequest
  case class GetNumbers(divisibleBy: Int) extends SimpleRequest
}
