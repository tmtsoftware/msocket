package msocket.simple.server

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.simple.api.WebsocketRequest._
import csw.simple.api.{SimpleApi, WebsocketRequest}
import mscoket.impl.ToPayload._
import msocket.api.RequestHandler

import scala.concurrent.ExecutionContext

class SimpleWebsocketRequestHandler(simpleApi: SimpleApi)(implicit ec: ExecutionContext, mat: Materializer)
    extends RequestHandler[WebsocketRequest, Source[Message, NotUsed]] {

  override def handle(message: WebsocketRequest): Source[Message, NotUsed] = message match {
    case Square(number)          => simpleApi.square(number).payload
    case GetNames(size)          => simpleApi.getNames(size).payloads
    case GetNumbers(divisibleBy) => simpleApi.getNumbers(divisibleBy).resultPayloads
  }
}
