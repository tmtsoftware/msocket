package csw.example.api

import com.github.ghik.silencer.silent
import csw.example.api.ExampleRequest._
import io.bullet.borer.derivation.MapBasedCodecs._
import io.bullet.borer.{Codec, Json, Target}

trait Codecs {
  implicit val target: Target = Json

  implicit def websocketRequestCodec[T <: ExampleRequest]: Codec[T] = exampleCodecValuealue.asInstanceOf[Codec[T]]

  lazy val exampleCodecValuealue: Codec[ExampleRequest] = {
    @silent implicit lazy val helloCodec: Codec[Hello]             = deriveCodec[Hello]
    @silent implicit lazy val squareCodec: Codec[Square]           = deriveCodec[Square]
    @silent implicit lazy val helloStreamCodec: Codec[HelloStream] = deriveCodec[HelloStream]
    @silent implicit val getNumbersCodec: Codec[GetNumbers]        = deriveCodec[GetNumbers]
    deriveCodec[ExampleRequest]
  }
}
