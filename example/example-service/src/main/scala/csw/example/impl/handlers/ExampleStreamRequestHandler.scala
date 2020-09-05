package csw.example.impl.handlers

import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleProtocol._
import msocket.security.api.AuthorizationPolicy.AuthorizedPolicy
import msocket.jvm.stream.{StreamRequestHandler, StreamResponse}

import scala.concurrent.Future

/**
 * Implements StreamRequestHandler for all requestStream messages in the protocol
 */
class ExampleStreamRequestHandler(exampleApi: ExampleApi) extends StreamRequestHandler[ExampleStreamRequest] {
  override def handle(message: ExampleStreamRequest): Future[StreamResponse] =
    message match {
      case Square(number)          => future(exampleApi.square(number))
      case HelloStream(name)       => stream(exampleApi.helloStream(name))
      case GetNumbers(divisibleBy) => stream(exampleApi.getNumbers(divisibleBy), policy = AuthorizedPolicy("ESW-User"))
      case RandomBagStream         => stream(exampleApi.randomBagStream())
    }
}
