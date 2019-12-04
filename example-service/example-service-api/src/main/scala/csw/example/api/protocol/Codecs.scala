package csw.example.api.protocol

import com.github.ghik.silencer.silent
import csw.example.api.protocol.ExampleError.{GetNumbersError, HelloError}
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream, Square}
import io.bullet.borer.Codec
import io.bullet.borer.derivation.ArrayBasedCodecs
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

trait Codecs {
  implicit def websocketRequestCodec[T <: ExampleRequest]: Codec[T] = exampleCodecValuealue.asInstanceOf[Codec[T]]

  lazy val exampleCodecValuealue: Codec[ExampleRequest] = {
    @silent implicit lazy val helloCodec: Codec[Hello]             = deriveCodec
    @silent implicit lazy val squareCodec: Codec[Square]           = deriveCodec
    @silent implicit lazy val helloStreamCodec: Codec[HelloStream] = deriveCodec
    @silent implicit val getNumbersCodec: Codec[GetNumbers]        = deriveCodec
    deriveCodec
  }

  implicit def exampleErrorCodec[T <: ExampleError]: Codec[T] = exampleErrorValue.asInstanceOf[Codec[T]]

  lazy val exampleErrorValue: Codec[ExampleError] = {
    @silent implicit lazy val helloCodec: Codec[HelloError]       = ArrayBasedCodecs.deriveUnaryCodec
    @silent implicit lazy val squareCodec: Codec[GetNumbersError] = ArrayBasedCodecs.deriveUnaryCodec
    deriveCodec
  }
}
