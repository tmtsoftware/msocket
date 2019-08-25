package csw.simple.api

import com.github.ghik.silencer.silent
import csw.simple.api.RequestProtocol._
import io.bullet.borer.derivation.MapBasedCodecs._
import io.bullet.borer.{Codec, Json, Target}

trait Codecs {
  implicit val target: Target = Json

  implicit def protocolCodec[T <: RequestProtocol]: Codec[T] = explicitProtocolCodec.asInstanceOf[Codec[T]]

  lazy val explicitProtocolCodec: Codec[RequestProtocol] = {
    @silent implicit lazy val helloCodec: Codec[Hello]   = deriveCodec[Hello]
    @silent implicit lazy val squareCodec: Codec[Square] = deriveCodec[Square]

    @silent implicit val getNamesCodec: Codec[GetNames]     = deriveCodec[GetNames]
    @silent implicit val getNumbersCodec: Codec[GetNumbers] = deriveCodec[GetNumbers]

    deriveCodec[RequestProtocol]
  }
}
