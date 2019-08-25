package csw.simple.api

import com.github.ghik.silencer.silent
import csw.simple.api.PostRequest.{Hello, HelloStream}
import csw.simple.api.WebsocketRequest._
import io.bullet.borer.derivation.MapBasedCodecs._
import io.bullet.borer.{Codec, Json, Target}

trait Codecs {
  implicit val target: Target = Json

  implicit def websocketRequestCodec[T <: WebsocketRequest]: Codec[T] = websocketRequestCodecvalue.asInstanceOf[Codec[T]]

  lazy val websocketRequestCodecvalue: Codec[WebsocketRequest] = {
    @silent implicit lazy val squareCodec: Codec[Square] = deriveCodec[Square]

    @silent implicit val getNamesCodec: Codec[GetNames]     = deriveCodec[GetNames]
    @silent implicit val getNumbersCodec: Codec[GetNumbers] = deriveCodec[GetNumbers]

    deriveCodec[WebsocketRequest]
  }

  implicit def postRequestCodec[T <: PostRequest]: Codec[T] = postRequestCodecValue.asInstanceOf[Codec[T]]

  lazy val postRequestCodecValue: Codec[PostRequest] = {
    @silent implicit lazy val helloCodec: Codec[Hello]             = deriveCodec[Hello]
    @silent implicit lazy val helloStreamCodec: Codec[HelloStream] = deriveCodec[HelloStream]
    deriveCodec[PostRequest]
  }

  implicit lazy val helloStreamResponseCodec: Codec[HelloStreamResponse] = deriveCodec[HelloStreamResponse]
}
