package csw.simple.api

sealed trait RequestProtocol

object RequestProtocol {
  case class Hello(name: String)          extends RequestProtocol
  case class Square(number: Int)          extends RequestProtocol
  case class GetNames(size: Int)          extends RequestProtocol
  case class GetNumbers(divisibleBy: Int) extends RequestProtocol
}
