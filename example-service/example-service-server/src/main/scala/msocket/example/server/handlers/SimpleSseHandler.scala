package msocket.example.server.handlers

import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.Materializer
import csw.example.api.ExampleRequest._
import csw.example.api.{ExampleApi, ExampleRequest}
import mscoket.impl.sse.SseStreamExtensions
import msocket.api.RequestHandler

class ExampleSseHandler(exampleApi: ExampleApi)(implicit mat: Materializer)
    extends RequestHandler[ExampleRequest, StandardRoute]
    with SseStreamExtensions {

  override def handle(message: ExampleRequest): StandardRoute = message match {
    case Hello(name)             => complete(futureAsStream(exampleApi.hello(name)))
    case Square(number)          => complete(futureAsStream(exampleApi.square(number)))
    case HelloStream(name)       => complete(stream(exampleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(streamWithError(exampleApi.getNumbers(divisibleBy)))
  }
}