package msocket.api.utils

import com.github.ghik.silencer.silent
import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

sealed trait StreamStatus
case object StreamSuccess                             extends StreamStatus
case class StreamError(name: String, message: String) extends StreamStatus

object StreamStatus {
  implicit def streamErrorCodec[T <: StreamStatus]: Codec[T] = streamErrorCodecValue.asInstanceOf[Codec[T]]

  lazy val streamErrorCodecValue: Codec[StreamStatus] = {
    @silent implicit lazy val streamErrorCodec: Codec[StreamError]          = deriveCodec[StreamError]
    @silent implicit lazy val streamSuccessCodec: Codec[StreamSuccess.type] = deriveCodec[StreamSuccess.type]
    deriveCodec[StreamStatus]
  }
}
