package csw.example.api.protocol

sealed trait ExampleRequest

object ExampleRequest {
  case class Hello(name: String)          extends ExampleRequest
  case class HelloStream(name: String)    extends ExampleRequest
  case class Square(number: Int)          extends ExampleRequest
  case class GetNumbers(divisibleBy: Int) extends ExampleRequest
}
