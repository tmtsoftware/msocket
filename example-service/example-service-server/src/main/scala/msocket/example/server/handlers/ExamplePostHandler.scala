package msocket.example.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import csw.aas.http.AuthorizationPolicy.{CustomPolicy, EmptyPolicy, RealmRolePolicy}
import csw.aas.http.SecurityDirectives
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleRequest
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import mscoket.impl.HttpCodecs
import mscoket.impl.post.PostStreamExtensions
import msocket.api.MessageHandler

class ExamplePostHandler(exampleApi: ExampleApi, securityDirectives: SecurityDirectives)(implicit mat: Materializer)
    extends MessageHandler[ExampleRequest, Route]
    with HttpCodecs
    with PostStreamExtensions {

  import securityDirectives._

  override def handle(request: ExampleRequest): Route = request match {
    case Hello(name) =>
      secure(RealmRolePolicy("marvel")) {
        complete(exampleApi.hello(name))
      }
    case Square(number)          => complete(futureAsStream(exampleApi.square(number)))
    case HelloStream(name)       => complete(stream(exampleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(streamWithError(exampleApi.getNumbers(divisibleBy)))
  }
}
