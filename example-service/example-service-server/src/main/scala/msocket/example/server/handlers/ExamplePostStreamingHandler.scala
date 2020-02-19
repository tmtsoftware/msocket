package msocket.example.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, RandomBag, RandomBagStream, Square}
import msocket.impl.post.{HttpPostHandler, ServerHttpCodecs}

/**
 * Implements HttpPostHandler for all messages in the protocol (requestResponse + requestStream)
 * These handlers receive POST requests and responds via [[Route]] instance
 */
class ExamplePostStreamingHandler(exampleApi: ExampleApi) extends HttpPostHandler[ExampleRequest] with ServerHttpCodecs {
  override def handle(request: ExampleRequest): Route = request match {
    // requestResponse interactions
    case Hello(name) => complete(exampleApi.hello(name))
    case RandomBag   => complete(exampleApi.randomBag())

    // requestStream interactions
    case Square(number)          => complete(stream(exampleApi.square(number))) // streams a Future returned by square, see square API docs
    case HelloStream(name)       => complete(stream(exampleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(stream(exampleApi.getNumbers(divisibleBy)))
    case RandomBagStream         => complete(stream(exampleApi.randomBagStream()))
  }
}
