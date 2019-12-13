package msocket.example.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import msocket.api.MessageHandler
import msocket.impl.post.{PostStreamExtensions, ServerHttpCodecs}

class ExamplePostHandler(exampleApi: ExampleApi)
    extends MessageHandler[ExampleRequest, Route]
    with ServerHttpCodecs
    with PostStreamExtensions {

  override def handle(request: ExampleRequest): Route = request match {
    case Hello(name)             => complete(exampleApi.hello(name))
    case Square(number)          => complete(futureAsStream(exampleApi.square(number)))
    case HelloStream(name)       => complete(stream(exampleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(stream(exampleApi.getNumbers(divisibleBy)))
  }
}
