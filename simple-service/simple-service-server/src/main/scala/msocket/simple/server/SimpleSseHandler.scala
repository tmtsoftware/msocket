package msocket.simple.server

import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.Materializer
import csw.simple.api.StreamRequest._
import csw.simple.api.{SimpleApi, StreamRequest}
import mscoket.impl.SseStreamExtensions
import msocket.api.RequestHandler

class SimpleSseHandler(simpleApi: SimpleApi)(implicit mat: Materializer)
    extends RequestHandler[StreamRequest, StandardRoute]
    with SseStreamExtensions {

  override def handle(message: StreamRequest): StandardRoute = message match {
    case Square(number)          => complete(stream(simpleApi.square(number)))
    case GetNames(size)          => complete(stream(simpleApi.getNames(size)))
    case GetNumbers(divisibleBy) => complete(streamWithError(simpleApi.getNumbers(divisibleBy)))
  }
}
