package msocket.example.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Juggle, JuggleStream, Square}
import msocket.impl.post.{HttpPostHandler, ServerHttpCodecs}

class ExamplePostStreamingHandler(exampleApi: ExampleApi) extends HttpPostHandler[ExampleRequest] with ServerHttpCodecs {
  override def handle(request: ExampleRequest): Route = request match {
    case Hello(name)             => complete(exampleApi.hello(name))
    case Square(number)          => complete(futureAsStream(exampleApi.square(number)))
    case HelloStream(name)       => complete(stream(exampleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(stream(exampleApi.getNumbers(divisibleBy)))
    case Juggle(bag)             => complete(exampleApi.juggle(bag))
    case JuggleStream(bag)       => complete(stream(exampleApi.juggleStream(bag)))
  }
}
