package msocket.simple.server

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.simple.api.StreamRequest._
import csw.simple.api.{SimpleApi, StreamRequest}
import mscoket.impl.ToPayload._
import msocket.api.RequestHandler

import scala.concurrent.ExecutionContext

class SimpleWebsocketRequestHandler(simpleApi: SimpleApi)(implicit ec: ExecutionContext, mat: Materializer)
    extends RequestHandler[StreamRequest, Source[Message, NotUsed]] {

  override def handle(message: StreamRequest): Source[Message, NotUsed] = message match {
    case Square(number)          => simpleApi.square(number).payload
    case GetNames(size)          => simpleApi.getNames(size).payloads
    case GetNumbers(divisibleBy) => simpleApi.getNumbers(divisibleBy).resultPayloads
  }
}
