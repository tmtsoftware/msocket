package msocket.simple.server

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.simple.api.RequestProtocol._
import csw.simple.api.{RequestProtocol, SimpleApi}
import msocket.core.api.Payload
import msocket.core.api.ToResponse.{FutureToPayload, SourceToPayload, SourceWithErrorToPayload}
import msocket.core.server.ServerSocket

import scala.concurrent.ExecutionContext

class SimpleServerSocket(simpleApi: SimpleApi)(implicit ec: ExecutionContext, mat: Materializer) extends ServerSocket[RequestProtocol] {

  override def requestStream(message: RequestProtocol): Source[Payload[_], NotUsed] = message match {
    case Hello(name)             => simpleApi.hello(name).payload
    case Square(number)          => simpleApi.square(number).payload
    case GetNames(size)          => simpleApi.getNames(size).payloads
    case GetNumbers(divisibleBy) => simpleApi.getNumbers(divisibleBy).resultPayloads
  }
}
