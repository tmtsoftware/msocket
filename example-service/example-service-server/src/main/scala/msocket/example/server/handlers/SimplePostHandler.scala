package msocket.example.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.Materializer
import csw.example.api.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import csw.example.api.{ExampleApi, ExampleRequest}
import mscoket.impl.HttpCodecs
import mscoket.impl.post.PostStreamExtensions
import msocket.api.RequestHandler

class ExamplePostHandler(exampleApi: ExampleApi)(implicit mat: Materializer)
    extends RequestHandler[ExampleRequest, StandardRoute]
    with HttpCodecs
    with PostStreamExtensions {

  override def handle(request: ExampleRequest): StandardRoute = request match {
    case Hello(name)             => complete(exampleApi.hello(name))
    case Square(number)          => complete(futureAsStream(exampleApi.square(number)))
    case HelloStream(name)       => complete(stream(exampleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(streamWithError(exampleApi.getNumbers(divisibleBy)))
  }
}
