package msocket.js.rsocket

import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ContentType, ErrorProtocol, Subscription, Transport}
import typings.rsocketCore.mod

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class RSocketTransportFactoryJs {
  def connect[Req: Encoder: ErrorProtocol](uri: String, contentType: ContentType)(implicit
      ec: ExecutionContext,
      streamingDelay: FiniteDuration
  ): (Transport[Req], Subscription) = {
    val transport = contentType match {
      case ContentType.Json => new RSocketTransportJs(uri, JsonText, mod.Utf8Encoders)
      case ContentType.Cbor => new RSocketTransportJs(uri, CborNodeBuffer, mod.BufferEncoders)
    }
    (transport, transport.subscription())
  }
}

object RSocketTransportFactoryJs extends RSocketTransportFactoryJs
