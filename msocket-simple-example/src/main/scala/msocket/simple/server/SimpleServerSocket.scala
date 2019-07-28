package msocket.simple.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.simple.api.Protocol._
import csw.simple.api.SimpleApi
import msocket.core.api.{DoneCodec, Payload}
import msocket.core.api.ToResponse.{FutureToPayload, SourceToPayload}
import msocket.core.server.ServerSocket

import scala.concurrent.{ExecutionContext, Future}

class SimpleServerSocket(simpleApi: SimpleApi)(implicit ec: ExecutionContext)
    extends ServerSocket[RequestResponse, RequestStream]
    with DoneCodec {

  override def requestResponse(message: RequestResponse): Future[Payload[_]] = message match {
    case Hello(name)     => simpleApi.hello(name).response
    case Square(number)  =>
      val response = simpleApi.square(number).response
      println(s"*******************$response")
      response
    case Ping(msg)       => simpleApi.ping(msg).response
    case Publish(number) => simpleApi.publish(number).response
  }

  override def requestStream(message: RequestStream): Source[Payload[_], NotUsed] = message match {
    case GetNames(size)          => simpleApi.getNames(size).responses
    case GetNumbers(divisibleBy) => simpleApi.getNumbers(divisibleBy).responses
  }
}
