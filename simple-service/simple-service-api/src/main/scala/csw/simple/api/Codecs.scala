package csw.simple.api

import com.github.ghik.silencer.silent
import csw.simple.api.SimpleRequest._
import io.bullet.borer.derivation.MapBasedCodecs._
import io.bullet.borer.{Codec, Json, Target}

trait Codecs {
  implicit val target: Target = Json

  implicit def websocketRequestCodec[T <: SimpleRequest]: Codec[T] = simpleCodecValuealue.asInstanceOf[Codec[T]]

  lazy val simpleCodecValuealue: Codec[SimpleRequest] = {
    @silent implicit lazy val helloCodec: Codec[Hello]             = deriveCodec[Hello]
    @silent implicit lazy val squareCodec: Codec[Square]           = deriveCodec[Square]
    @silent implicit lazy val helloStreamCodec: Codec[HelloStream] = deriveCodec[HelloStream]
    @silent implicit val getNumbersCodec: Codec[GetNumbers]        = deriveCodec[GetNumbers]
    deriveCodec[SimpleRequest]
  }
}
