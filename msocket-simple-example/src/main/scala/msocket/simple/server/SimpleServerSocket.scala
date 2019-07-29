package msocket.simple.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.simple.api.Protocol._
import csw.simple.api.{Protocol, SimpleApi}
import msocket.core.api.Payload
import msocket.core.api.ToResponse.{FutureToPayload, SourceToPayload}
import msocket.core.server.ServerSocket

import scala.concurrent.ExecutionContext

class SimpleServerSocket(simpleApi: SimpleApi)(implicit ec: ExecutionContext) extends ServerSocket[Protocol] {

  override def requestStream(message: Protocol): Source[Payload[_], NotUsed] = message match {
    case Hello(name)             => simpleApi.hello(name).payload
    case Square(number)          => simpleApi.square(number).payload
    case GetNames(size)          => simpleApi.getNames(size).payloads
    case GetNumbers(divisibleBy) => simpleApi.getNumbers(divisibleBy).payloads
  }

}
