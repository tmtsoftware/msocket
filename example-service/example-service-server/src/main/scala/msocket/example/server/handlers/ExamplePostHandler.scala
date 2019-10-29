package msocket.example.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import csw.aas.http.AuthorizationPolicy.RealmRolePolicy
import csw.aas.http.SecurityDirectives
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import mscoket.impl.post.{PostStreamExtensions, ServerHttpCodecs}
import msocket.api.MessageHandler

class ExamplePostHandler(exampleApi: ExampleApi, securityDirectives: SecurityDirectives)(implicit mat: Materializer)
    extends MessageHandler[ExampleRequest, Route]
    with ServerHttpCodecs
    with PostStreamExtensions {

  import securityDirectives._

  override def handle(request: ExampleRequest): Route = request match {
    case Hello(name) =>
      secure(RealmRolePolicy("marvel")) {
        complete(exampleApi.hello(name))
      }
    case Square(number)          => complete(futureAsStream(exampleApi.square(number)))
    case HelloStream(name)       => complete(stream(exampleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(streamWithStatus(exampleApi.getNumbers(divisibleBy)))
  }
}
