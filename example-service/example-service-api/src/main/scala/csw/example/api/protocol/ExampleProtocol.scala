package csw.example.api.protocol

import msocket.api.Labelled

/**
 * Transport agnostic message protocol is defined as a Scala ADT
 * For each API method, there needs to be a matching message in this ADT
 * For example, Hello(name) is the message for hello(name) method in the API
 */
object ExampleProtocol {
  // these messages are used for requestResponse interaction model
  sealed trait ExampleRequest
  case class Hello(name: String) extends ExampleRequest
  case object RandomBag          extends ExampleRequest

  object ExampleRequest {
    private val testLabel                           = "test_label"
    implicit val labelled: Labelled[ExampleRequest] =
      Labelled.make(List(testLabel)) { case _ => Map(testLabel -> "test_value") }
  }

  // these messages are used for requestStream interaction model
  sealed trait ExampleStreamRequest
  case class HelloStream(name: String)    extends ExampleStreamRequest
  case class Square(number: Int)          extends ExampleStreamRequest
  case class GetNumbers(divisibleBy: Int) extends ExampleStreamRequest
  case object RandomBagStream             extends ExampleStreamRequest
}
