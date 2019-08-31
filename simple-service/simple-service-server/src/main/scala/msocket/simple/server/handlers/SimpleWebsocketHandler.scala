package msocket.simple.server.handlers

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.simple.api.SimpleRequest._
import csw.simple.api.{SimpleApi, SimpleRequest}
import mscoket.impl.ws.WebsocketStreamExtensions
import msocket.api.RequestHandler

class SimpleWebsocketHandler(simpleApi: SimpleApi)(implicit mat: Materializer)
    extends RequestHandler[SimpleRequest, Source[Message, NotUsed]]
    with WebsocketStreamExtensions {

  override def handle(message: SimpleRequest): Source[Message, NotUsed] = message match {
    case Hello(name)             => futureAsStream(simpleApi.hello(name))
    case Square(number)          => futureAsStream(simpleApi.square(number))
    case HelloStream(name)       => stream(simpleApi.helloStream(name))
    case GetNumbers(divisibleBy) => streamWithError(simpleApi.getNumbers(divisibleBy))
  }
}
