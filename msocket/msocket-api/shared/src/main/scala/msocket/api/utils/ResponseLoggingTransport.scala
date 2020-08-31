package msocket.api.utils

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.models.Headers
import msocket.api.{ErrorProtocol, LoggingMessageEncoder, Subscription, Transport}
import msocket.portable.{Observer, PortableAkka}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ResponseLoggingTransport[Req: Encoder](transport: Transport[Req], action: String => Unit = println)(implicit
    ec: ExecutionContext,
    ep: ErrorProtocol[Req]
) extends Transport {

  private val loggingEncoder = new LoggingMessageEncoder[Req](action)

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] =
    transport.requestResponse(request).transform(logMessage[Res])

  override def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res] =
    transport.requestResponse(request, timeout).transform(logMessage[Res])

  override def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription] = {
    val observer = Observer.create[Res](out => loggingEncoder.encode(out, Headers()), loggingEncoder.errorEncoder)
    PortableAkka.viaObserver(transport.requestStream(request), observer)
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, observer: Observer[Res]): Subscription = {
    val combinedObserver = Observer.from[Res] { x =>
      loggingObserver[Res].run(x)
      observer.run(x)
    }
    transport.requestStream(request, combinedObserver)
  }

  def loggingObserver[Res: Encoder]: Observer[Res] =
    new Observer[Res] {
      override def onNext(elm: Res): Unit       = loggingEncoder.encode(elm, Headers())
      override def onError(ex: Throwable): Unit = loggingEncoder.errorEncoder(ex)
      override def onCompleted(): Unit          = action("stream completed")
    }

  private def logMessage[Res: Encoder](response: Try[Res]): Try[Res] = {
    loggingObserver[Res].run(response.map(Some(_)))
    response
  }
}
