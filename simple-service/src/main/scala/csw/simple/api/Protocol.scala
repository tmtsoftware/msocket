package csw.simple.api

sealed trait Protocol

object Protocol {
  case class Hello(name: String)          extends Protocol
  case class Square(number: Int)          extends Protocol
  case class GetNames(size: Int)          extends Protocol
  case class GetNumbers(divisibleBy: Int) extends Protocol
}
