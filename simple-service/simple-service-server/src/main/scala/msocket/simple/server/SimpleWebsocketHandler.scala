package msocket.simple.server

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.simple.api.WebsocketRequest._
import csw.simple.api.{WebsocketRequest, SimpleApi}
import msocket.api.{Payload, WebsocketHandler}
import mscoket.impl.ToPayload._

import scala.concurrent.ExecutionContext

class SimpleWebsocketHandler(simpleApi: SimpleApi)(implicit ec: ExecutionContext, mat: Materializer)
    extends WebsocketHandler[WebsocketRequest] {

  override def handle(message: WebsocketRequest): Source[Payload[_], NotUsed] = message match {
    case Square(number)          => simpleApi.square(number).payload
    case GetNames(size)          => simpleApi.getNames(size).payloads
    case GetNumbers(divisibleBy) => simpleApi.getNumbers(divisibleBy).resultPayloads
  }
}
