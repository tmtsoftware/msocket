package msocket.example.server.handlers

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, HelloStream, Square}
import io.rsocket.Payload
import msocket.api.Encoding
import msocket.impl.rsocket.server.RSocketStreamHandler

class ExampleRSocketStreamHandler(exampleApi: ExampleApi, encoding: Encoding[_]) extends RSocketStreamHandler[ExampleRequest](encoding) {
  override def handle(message: ExampleRequest): Source[Payload, NotUsed] = message match {
    case Square(number)          => futureAsStream(exampleApi.square(number))
    case HelloStream(name)       => stream(exampleApi.helloStream(name))
    case GetNumbers(divisibleBy) => stream(exampleApi.getNumbers(divisibleBy))
    case _                       => Source.failed(new RuntimeException("request-response is not supported by request-stream handler"))
  }
}
