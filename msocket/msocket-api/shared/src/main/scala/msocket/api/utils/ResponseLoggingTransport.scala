package msocket.api.utils

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.models.Headers
import msocket.api.{ErrorProtocol, LoggingMessageEncoder, Subscription, Transport}
import portable.akka.extensions.PortableAkka

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class ResponseLoggingTransport[Req: Encoder](transport: Transport[Req], action: String => Unit = println)(implicit
    ec: ExecutionContext,
    ep: ErrorProtocol[Req]
) extends Transport {

  private val loggingEncoder = new LoggingMessageEncoder[Req](action)

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] =
    transport.requestResponse(request).transform(logResponseTry[Res])

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] =
    transport.requestResponse(request, timeout).transform(logResponseTry[Res])

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] =
    PortableAkka.withEffects(transport.requestStream(request))(
      out => loggingEncoder.encode(out, Headers()),
      loggingEncoder.errorEncoder
    )

  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Try[Option[Res]] => Unit): Subscription = {
    transport.requestStream(request, logMessage[Res] _ andThen onMessage)
  }

  def logMessage[Res: Encoder](msg: Try[Option[Res]]): Try[Option[Res]] = {
    msg match {
      case Failure(exception)   => Failure(logError(exception))
      case Success(Some(value)) => Success(Some(logResponse(value)))
      case Success(None)        => Success({ action("stream completed"); None })
    }
  }

  private def logResponseTry[Res: Encoder](response: Try[Res]): Try[Res] = logMessage(response.map(Some(_))).map(_.get)

  private def logResponse[Res: Encoder](response: Res): Res = {
    loggingEncoder.encode(response, Headers())
    response
  }

  private def logError[Res](ex: Throwable): Throwable = {
    loggingEncoder.errorEncoder(ex)
    ex
  }
}
