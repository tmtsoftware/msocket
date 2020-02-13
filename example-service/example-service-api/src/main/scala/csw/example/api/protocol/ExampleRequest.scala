package csw.example.api.protocol

/**
 * Transport agnostic message protocol is defined as a Scala ADT
 * For each API method, there needs to be a matching message in this ADT
 * For example, Hello(name) is the message for hello(name) method in the API
 */
sealed trait ExampleRequest

object ExampleRequest {
  sealed trait ExampleRequestResponse extends ExampleRequest
  case class Hello(name: String)      extends ExampleRequestResponse
  case object RandomBag               extends ExampleRequestResponse

  sealed trait ExampleRequestStream       extends ExampleRequest
  case class HelloStream(name: String)    extends ExampleRequestStream
  case class Square(number: Int)          extends ExampleRequestStream
  case class GetNumbers(divisibleBy: Int) extends ExampleRequestStream
  case object RandomBagStream             extends ExampleRequestStream
}
