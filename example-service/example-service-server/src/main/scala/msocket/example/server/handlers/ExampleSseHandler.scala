package msocket.example.server.handlers

import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import msocket.impl.sse.SseHandler

class ExampleSseHandler(exampleApi: ExampleApi) extends SseHandler[ExampleRequest] {

  override def handle(message: ExampleRequest): StandardRoute = message match {
    case Hello(name)    => complete(futureAsStream(exampleApi.hello(name)))
    case Square(number) => complete(futureAsStream(exampleApi.square(number)))

    case HelloStream(name)       => complete(stream(exampleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(stream(exampleApi.getNumbers(divisibleBy)))
  }
}
