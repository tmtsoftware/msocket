package msocket.example.server.handlers

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleRequest._
import io.rsocket.Payload
import msocket.api.ContentType
import msocket.impl.rsocket.server.RSocketStreamHandler

/**
 * Implements RSocketStreamHandler for all requestStream messages in the protocol
 * These handlers handle RSocket's requestStream interaction model and returns a [[Source]] of [[Payload]]
 */
class ExampleRSocketStreamHandler(exampleApi: ExampleApi, contentType: ContentType)
    extends RSocketStreamHandler[ExampleRequestStream](contentType) {
  override def handle(message: ExampleRequestStream): Source[Payload, NotUsed] =
    message match {
      case Square(number)          => stream(exampleApi.square(number))
      case HelloStream(name)       => stream(exampleApi.helloStream(name))
      case GetNumbers(divisibleBy) => stream(exampleApi.getNumbers(divisibleBy))
      case RandomBagStream         => stream(exampleApi.randomBagStream())
    }
}
