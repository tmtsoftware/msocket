package csw.example.api.handlers

import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleProtocol._
import msocket.api.{StreamRequestHandler, StreamResponse}

/**
 * Implements StreamRequestHandler for all requestStream messages in the protocol
 */
class ExampleStreamHandler(exampleApi: ExampleApi) extends StreamRequestHandler[ExampleStreamRequest] {
  override def handle(message: ExampleStreamRequest): StreamResponse =
    message match {
      case Square(number)          => future(exampleApi.square(number))
      case HelloStream(name)       => stream(exampleApi.helloStream(name))
      case GetNumbers(divisibleBy) => stream(exampleApi.getNumbers(divisibleBy))
      case RandomBagStream         => stream(exampleApi.randomBagStream())
    }
}
