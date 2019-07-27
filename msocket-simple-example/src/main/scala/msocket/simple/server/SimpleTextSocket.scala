package msocket.simple.server

import java.util.UUID

import akka.http.scaladsl.model.ws.TextMessage
import csw.simple.api.Protocol._
import csw.simple.api.SimpleApi
import io.bullet.borer.Target
import msocket.core.api.TextSocket
import msocket.core.extensions.ToPayload.{FutureToPayload, SourceToPayload}

import scala.concurrent.{ExecutionContext, Future}

class SimpleTextSocket(simpleApi: SimpleApi)(implicit ec: ExecutionContext, target: Target)
    extends TextSocket[RequestResponse, RequestStream] {
  override def requestResponse(message: RequestResponse, id: UUID): Future[TextMessage.Strict] = message match {
    case Hello(name)    => simpleApi.hello(name).payloadTextMessage(id)
    case Square(number) => simpleApi.square(number).payloadTextMessage(id)
  }

  override def requestStream(message: RequestStream, id: UUID): TextMessage.Streamed = message match {
    case GetNames(size)          => simpleApi.getNames(size).payloadTextMessage(id)
    case GetNumbers(divisibleBy) => simpleApi.getNumbers(divisibleBy).payloadTextMessage(id)
  }
}
