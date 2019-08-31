package msocket.example.server.handlers

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.example.api.ExampleRequest._
import csw.example.api.{ExampleApi, ExampleRequest}
import mscoket.impl.ws.WebsocketStreamExtensions
import msocket.api.RequestHandler

class ExampleWebsocketHandler(exampleApi: ExampleApi)(implicit mat: Materializer)
    extends RequestHandler[ExampleRequest, Source[Message, NotUsed]]
    with WebsocketStreamExtensions {

  override def handle(message: ExampleRequest): Source[Message, NotUsed] = message match {
    case Hello(name)             => futureAsStream(exampleApi.hello(name))
    case Square(number)          => futureAsStream(exampleApi.square(number))
    case HelloStream(name)       => stream(exampleApi.helloStream(name))
    case GetNumbers(divisibleBy) => streamWithError(exampleApi.getNumbers(divisibleBy))
  }
}
