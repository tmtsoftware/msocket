package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

sealed trait StreamStatus

case class StreamStarted(subscription: Subscription) extends StreamStatus {
  override def toString: String = "stream started"
}

case class StreamError(name: String, message: String) extends StreamStatus

object StreamError {
  implicit lazy val streamErrorCodec: Codec[StreamError] = deriveCodec[StreamError]
}
