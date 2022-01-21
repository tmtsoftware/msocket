package msocket.api

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.utils.{ContraMappedTransport, ResponseLoggingTransport}
import msocket.portable.Observer

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

/**
 * Transport API abstracts 2 main interaction models: requestResponse and requestStream with their variants Implementations of Transport API
 * will be provided by msocket-impl* modules.
 */
abstract class Transport[Req: Encoder: ErrorProtocol] {

  /**
   * Send a request and receive a [[Future]] of response. Only supported by transports that support implicit connection timeouts if the
   * response is not obtained within that timeout
   */
  def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res]

  /**
   * Send a request and receive a [[Future]] of response within a specified timeout
   */
  def requestResponse[Res: Decoder: Encoder](request: Req, timeout: FiniteDuration): Future[Res]

  /**
   * Send a request and receive a streaming [[Source]] of response elements The given [[Source]] will materialize to a [[Subscription]] that
   * can be used for cancellation
   */
  def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, Subscription]

  /**
   * Send a request and invoke onMessage callback on each element of the streaming response and invoke onError callback on receiving an
   * error which will terminate the stream The given [[Source]] will materialize to a [[Subscription]] that can be used for cancellation
   */
  def requestStream[Res: Decoder: Encoder](request: Req, observer: Observer[Res]): Subscription
}

object Transport {

  implicit class TransportOps[Req: Encoder: ErrorProtocol](transport: Transport[Req]) {
    def contraMap[T: Encoder: ErrorProtocol](action: T => Req): Transport[T] = {
      new ContraMappedTransport(transport, action)
    }

    def withEffect(action: Req => Unit): Transport[Req] =
      contraMap { x =>
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
