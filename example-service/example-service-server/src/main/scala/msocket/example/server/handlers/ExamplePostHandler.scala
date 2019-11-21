package msocket.example.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import csw.aas.http.AuthorizationPolicy.RealmRolePolicy
import csw.aas.http.SecurityDirectives
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import msocket.api.MessageHandler
import msocket.impl.post.{PostStreamExtensions, ServerHttpCodecs}

class ExamplePostHandler(exampleApi: ExampleApi, securityDirectives: SecurityDirectives)
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
    case GetNumbers(divisibleBy) => complete(stream(exampleApi.getNumbers(divisibleBy)))
  }
}
