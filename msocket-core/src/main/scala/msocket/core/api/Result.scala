package msocket.core.api

import com.github.ghik.silencer.silent
import io.bullet.borer.{Codec, Decoder, Encoder}
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveUnaryCodec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

sealed trait Result[S, E]

object Result {
  case class Success[S, E](value: S) extends Result[S, E]
  case class Error[S, E](value: E)   extends Result[S, E]

  implicit def resultCodec[E: Encoder: Decoder, S: Encoder: Decoder]: Codec[Result[S, E]] = {
    @silent implicit lazy val errorCodec: Codec[Error[S, E]]     = deriveUnaryCodec[Error[S, E]]
    @silent implicit lazy val successCodec: Codec[Success[S, E]] = deriveUnaryCodec[Success[S, E]]
    deriveCodec[Result[S, E]]
  }
}
