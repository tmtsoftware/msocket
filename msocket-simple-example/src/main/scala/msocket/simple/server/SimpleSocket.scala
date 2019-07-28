package msocket.simple.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.simple.api.Protocol._
import csw.simple.api.SimpleApi
import msocket.core.api.{DoneCodec, MResponse, MSocket}
import msocket.core.extensions.ToResponse.{FutureToPayload, SourceToPayload}

import scala.concurrent.{ExecutionContext, Future}

class SimpleSocket(simpleApi: SimpleApi)(implicit ec: ExecutionContext)
    extends MSocket[RequestResponse, RequestStream]
    with DoneCodec {

  override def requestResponse(message: RequestResponse): Future[MResponse[_]] = message match {
    case Hello(name)     => simpleApi.hello(name).response
    case Square(number)  => simpleApi.square(number).response
    case Ping(msg)       => simpleApi.ping(msg).response
    case Publish(number) => simpleApi.publish(number).response
  }

  override def requestStream(message: RequestStream): Source[MResponse[_], NotUsed] = message match {
    case GetNames(size)          => simpleApi.getNames(size).responses
    case GetNumbers(divisibleBy) => simpleApi.getNumbers(divisibleBy).responses
  }
}
