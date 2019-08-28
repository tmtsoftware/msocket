package msocket.simple.server

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.simple.api.StreamRequest._
import csw.simple.api.{SimpleApi, StreamRequest}
import mscoket.impl.ws.WebsocketStreamExtensions
import msocket.api.RequestHandler

class SimpleWebsocketHandler(simpleApi: SimpleApi)(implicit mat: Materializer)
    extends RequestHandler[StreamRequest, Source[Message, NotUsed]]
    with WebsocketStreamExtensions {

  override def handle(message: StreamRequest): Source[Message, NotUsed] = message match {
    case Square(number)          => stream(simpleApi.square(number))
    case GetNames(size)          => stream(simpleApi.getNames(size))
    case GetNumbers(divisibleBy) => streamWithError(simpleApi.getNumbers(divisibleBy))
  }
}
