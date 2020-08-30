package msocket.impl.rsocket

import io.bullet.borer.Encoder
import msocket.api.ContentEncoding.JsonText
import msocket.api.{ContentType, ErrorProtocol, Transport}
import typings.rsocketCore.mod

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class RSocketTransportFactoryJs[Req: Encoder: ErrorProtocol] {
  def connect(uri: String, contentType: ContentType)(implicit ec: ExecutionContext, streamingDelay: FiniteDuration): Transport[Req] = {
    contentType match {
      case ContentType.Json => new RSocketTransportJs(uri, JsonText, mod.Utf8Encoders)
      case ContentType.Cbor => new RSocketTransportJs(uri, CborNodeBuffer, mod.BufferEncoders)
    }
  }
}
