package msocket.example.server.handlers

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, HelloStream}
import io.rsocket.Payload
import msocket.api.MessageHandler
import msocket.impl.rsocket.server.RSocketStreamExtensions

class ExampleRequestStreamHandler(exampleApi: ExampleApi)
    extends MessageHandler[ExampleRequest, Source[Payload, NotUsed]]
    with RSocketStreamExtensions {

  override def handle(message: ExampleRequest): Source[Payload, NotUsed] = message match {
    case HelloStream(name)       => stream(exampleApi.helloStream(name))
    case GetNumbers(divisibleBy) => stream(exampleApi.getNumbers(divisibleBy))
    case _                       => Source.failed(new RuntimeException("request-response is not supported bu request-stream handler"))
  }
}
