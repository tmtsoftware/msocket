package msocket.simple.server

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.simple.api.RequestProtocol._
import csw.simple.api.{RequestProtocol, SimpleApi}
import msocket.api.{Payload, WebsocketHandler}
import mscoket.impl.ToPayload._

import scala.concurrent.ExecutionContext

class SimpleServerSocket(simpleApi: SimpleApi)(implicit ec: ExecutionContext, mat: Materializer) extends WebsocketHandler[RequestProtocol] {

  override def handle(message: RequestProtocol): Source[Payload[_], NotUsed] = message match {
    case Hello(name)             => simpleApi.hello(name).payload
    case Square(number)          => simpleApi.square(number).payload
    case GetNames(size)          => simpleApi.getNames(size).payloads
    case GetNumbers(divisibleBy) => simpleApi.getNumbers(divisibleBy).resultPayloads
  }
}
