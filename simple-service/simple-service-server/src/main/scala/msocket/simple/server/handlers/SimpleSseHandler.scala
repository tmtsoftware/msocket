package msocket.simple.server.handlers

import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.toEventStream
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.Materializer
import csw.simple.api.SimpleRequest._
import csw.simple.api.{SimpleApi, SimpleRequest}
import mscoket.impl.sse.SseStreamExtensions
import msocket.api.RequestHandler

class SimpleSseHandler(simpleApi: SimpleApi)(implicit mat: Materializer)
    extends RequestHandler[SimpleRequest, StandardRoute]
    with SseStreamExtensions {

  override def handle(message: SimpleRequest): StandardRoute = message match {
    case Hello(name)             => complete(futureAsStream(simpleApi.hello(name)))
    case Square(number)          => complete(futureAsStream(simpleApi.square(number)))
    case HelloStream(name)       => complete(stream(simpleApi.helloStream(name)))
    case GetNumbers(divisibleBy) => complete(streamWithError(simpleApi.getNumbers(divisibleBy)))
  }
}
