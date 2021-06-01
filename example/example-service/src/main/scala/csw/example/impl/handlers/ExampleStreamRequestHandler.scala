package csw.example.impl.handlers

import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleProtocol._
import csw.example.impl.ExampleAuthorizationPolicy.AuthorizedPolicy
import msocket.jvm.stream.{StreamRequestHandler, StreamResponse}

import scala.concurrent.Future

/**
 * Implements StreamRequestHandler for all requestStream messages in the protocol
 */
class ExampleStreamRequestHandler(exampleApi: ExampleApi) extends StreamRequestHandler[ExampleStreamRequest] {
  override def handle(message: ExampleStreamRequest): Future[StreamResponse] =
    message match {
      case Square(number)          => response(exampleApi.square(number))
      case HelloStream(name)       => stream(exampleApi.helloStream(name))
      case GetNumbers(divisibleBy) => sStream(AuthorizedPolicy("ESW-User"))(_ => exampleApi.getNumbers(divisibleBy))
      case RandomBagStream         => stream(exampleApi.randomBagStream())
    }
}
