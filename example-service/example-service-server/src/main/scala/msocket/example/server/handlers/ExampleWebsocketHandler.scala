package msocket.example.server.handlers

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import mscoket.impl.ws.WebsocketStreamExtensions
import msocket.api.RequestHandler

class ExampleWebsocketHandler(exampleApi: ExampleApi)(implicit mat: Materializer)
    extends RequestHandler[ExampleRequest, Source[String, NotUsed]]
    with WebsocketStreamExtensions {

  override def handle(message: ExampleRequest): Source[String, NotUsed] = message match {
    case Hello(name)    => futureAsStream(exampleApi.hello(name))
    case Square(number) => futureAsStream(exampleApi.square(number))

    case HelloStream(name)       => stream(exampleApi.helloStream(name))
    case GetNumbers(divisibleBy) => streamWithError(exampleApi.getNumbers(divisibleBy))
  }
}
