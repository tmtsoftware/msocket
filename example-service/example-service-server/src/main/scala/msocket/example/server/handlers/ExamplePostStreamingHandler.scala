package msocket.example.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, RandomBag, RandomBagStream, Square}
import msocket.impl.post.{HttpPostHandler, ServerHttpCodecs}

/**
 * A HTTP handler that will create routes for defined APIs in [[ExampleApi]]
 *
 * This extends the [[HttpPostHandler]] and leverages its stream functionality for APIs
 * that return streaming response
 */
class ExamplePostStreamingHandler(exampleApi: ExampleApi) extends HttpPostHandler[ExampleRequest] with ServerHttpCodecs {
  override def handle(request: ExampleRequest): Route = request match {
    case Hello(name)             => complete(exampleApi.hello(name))
    case Square(number)          => complete(futureAsStream(exampleApi.square(number)))
    case HelloStream(name)       => complete(stream(exampleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(stream(exampleApi.getNumbers(divisibleBy)))
    case RandomBag               => complete(exampleApi.randomBag())
    case RandomBagStream         => complete(stream(exampleApi.randomBagStream()))
  }
}
