package msocket.api

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.utils.{ContraMappedTransport, ResponseLoggingTransport}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

abstract class Transport[Req: Encoder: ErrorProtocol] {
  def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res]
  def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res]
  def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription]
  def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Res => Unit, onError: Throwable => Unit): Subscription

}

object Transport {

  implicit class TransportOps[Req: Encoder: ErrorProtocol](transport: Transport[Req]) {
    def contraMap[T: Encoder: ErrorProtocol](action: T => Req): Transport[T] = {
      new ContraMappedTransport(transport, action)
    }

    def withEffect(action: Req => Unit): Transport[Req] = contraMap { x =>
      action(x)
      x
    }

    def logRequest(action: String => Unit = println): Transport[Req] = {
      withEffect(x => action(s"Request --> ${JsonText.encode(x)}"))
    }

    def logResponse(action: String => Unit = println)(implicit ec: ExecutionContext): Transport[Req] = {
      new ResponseLoggingTransport[Req](transport, action)
    }

    def logRequestResponse(action: String => Unit = println)(implicit ec: ExecutionContext): Transport[Req] = {
      logRequest(action).logResponse(action)
    }
  }
}
