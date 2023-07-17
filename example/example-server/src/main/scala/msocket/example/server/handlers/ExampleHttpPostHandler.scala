package msocket.example.server.handlers

import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, Hello, RandomBag}
import msocket.http.post.{HttpPostHandler, ServerHttpCodecs}

/**
 * Implements HttpPostHandler for all requestResponse messages in the protocol
 * These handlers receive POST requests and responds via [[Route]] instance
 */
class ExampleHttpPostHandler(exampleApi: ExampleApi) extends HttpPostHandler[ExampleRequest] with ServerHttpCodecs {
  override def handle(request: ExampleRequest): Route =
    request match {
      case Hello(name) => complete(exampleApi.hello(name))
      case RandomBag   => complete(exampleApi.randomBag())
    }
}
