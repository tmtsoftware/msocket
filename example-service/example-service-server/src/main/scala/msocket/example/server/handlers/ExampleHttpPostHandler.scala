package msocket.example.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleRequest.{ExampleRequestResponse, Hello, RandomBag}
import msocket.impl.post.{HttpPostHandler, ServerHttpCodecs}

/**
 * Implements HttpPostHandler for all messages in the protocol (requestResponse + requestStream)
 * These handlers receive POST requests and responds via [[Route]] instance
 */
class ExampleHttpPostHandler(exampleApi: ExampleApi) extends HttpPostHandler[ExampleRequestResponse] with ServerHttpCodecs {
  override def handle(request: ExampleRequestResponse): Route = request match {
    case Hello(name) => complete(exampleApi.hello(name))
    case RandomBag   => complete(exampleApi.randomBag())
  }
}
