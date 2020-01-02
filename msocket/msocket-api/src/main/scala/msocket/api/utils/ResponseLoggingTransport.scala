package msocket.api.utils

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.JsonText
import msocket.api.{ErrorProtocol, Subscription, Transport}
import portable.akka.extensions.PortableAkka

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

class ResponseLoggingTransport[Req: Encoder: ErrorProtocol](transport: Transport[Req], action: String => Unit = println)(
    implicit ec: ExecutionContext
) extends Transport {

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] =
    transport.requestResponse(request).map(x => log(x))

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] =
    transport.requestResponse(request, timeout).map(x => log(x))

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] =
    PortableAkka.withEffect(transport.requestStream(request))(x => log(x))

  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Res => Unit): Subscription =
    transport.requestStream(request, (x: Res) => onMessage(log(x)))

  private def log[Res: Decoder: Encoder](response: Res): Res = {
    action(s"Response <-- ${JsonText.encode(response)}")
    response
  }
}
