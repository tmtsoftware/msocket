package msocket.simple.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.Materializer
import csw.simple.api.SimpleRequest.{GetNumbers, Hello, HelloStream, Square}
import csw.simple.api.{SimpleApi, SimpleRequest}
import mscoket.impl.HttpCodecs
import mscoket.impl.post.PostStreamExtensions
import msocket.api.RequestHandler

class SimplePostHandler(simpleApi: SimpleApi)(implicit mat: Materializer)
    extends RequestHandler[SimpleRequest, StandardRoute]
    with HttpCodecs
    with PostStreamExtensions {

  override def handle(request: SimpleRequest): StandardRoute = request match {
    case Hello(name)             => complete(simpleApi.hello(name))
    case Square(number)          => complete(futureAsStream(simpleApi.square(number)))
    case HelloStream(name)       => complete(stream(simpleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(streamWithError(simpleApi.getNumbers(divisibleBy)))
  }
}
