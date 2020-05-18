package msocket.api.utils

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ErrorProtocol, LoggingMessageEncoder, Subscription, Transport}
import portable.akka.extensions.PortableAkka

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class ResponseLoggingTransport[Req: Encoder](transport: Transport[Req], action: String => Unit = println)(implicit
    ec: ExecutionContext,
    ep: ErrorProtocol[Req]
) extends Transport {

  private val loggingEncoder = new LoggingMessageEncoder[Req](action)

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] =
    transport.requestResponse(request).map(x => logMessage(x)).recover(logError)

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] =
    transport.requestResponse(request, timeout).map(x => logMessage(x)).recover(logError)

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] =
    PortableAkka.withEffects(transport.requestStream(request))(loggingEncoder.encode, loggingEncoder.errorEncoder)

  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Res => Unit, onError: Throwable => Unit): Subscription = {
    transport.requestStream(request, (x: Res) => onMessage(logMessage(x)), logError andThen onError)
  }

  private def logMessage[Res: Encoder](response: Res): Res = {
    loggingEncoder.encode(response)
    response
  }

  private def logError[Res]: PartialFunction[Throwable, Res] = {
    case ex => loggingEncoder.errorEncoder(ex); throw ex
  }
}
