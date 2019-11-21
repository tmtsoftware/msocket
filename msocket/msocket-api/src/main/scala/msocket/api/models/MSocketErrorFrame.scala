package msocket.api.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

case class MSocketErrorFrame(`stream-error`: StreamError) extends RuntimeException(`stream-error`.toString)

object MSocketErrorFrame {
  implicit lazy val MSocketErrorFrameCodec: Codec[MSocketErrorFrame] = deriveCodec
}
