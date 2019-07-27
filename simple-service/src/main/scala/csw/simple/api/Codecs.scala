package csw.simple.api

import akka.Done
import com.github.ghik.silencer.silent
import csw.simple.api.Protocol._
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveCodecForUnaryCaseClass
import io.bullet.borer.derivation.MapBasedCodecs._
import io.bullet.borer.{Cbor, Codec, Target}

trait Codecs {
  implicit val target: Target = Cbor

  implicit lazy val doneCodec: Codec[Done] = Codec.implicitly[String].bimap[Done](_ => "done", _ => Done)

  implicit def protocolCodec[T <: Protocol]: Codec[T] = deriveCodec[Protocol].asInstanceOf[Codec[T]]

  implicit def requestResponseCodec[T <: RequestResponse]: Codec[T] = {
    @silent implicit val helloCodec: Codec[Hello]   = deriveCodecForUnaryCaseClass[Hello]
    @silent implicit val squareCodec: Codec[Square] = deriveCodecForUnaryCaseClass[Square]
    deriveCodec[RequestResponse].asInstanceOf[Codec[T]]
  }

  implicit def requestStreamCodec[T <: RequestStream]: Codec[T] = {
    @silent implicit val getNamesCodec: Codec[GetNames]     = deriveCodecForUnaryCaseClass[GetNames]
    @silent implicit val getNumbersCodec: Codec[GetNumbers] = deriveCodecForUnaryCaseClass[GetNumbers]
    deriveCodec[RequestStream].asInstanceOf[Codec[T]]
  }

  implicit def fireAndForgetCodec[T <: FireAndForget]: Codec[T] = {
    @silent implicit val getNamesCodec: Codec[Ping]      = deriveCodecForUnaryCaseClass[Ping]
    @silent implicit val getNumbersCodec: Codec[Publish] = deriveCodecForUnaryCaseClass[Publish]
    deriveCodec[FireAndForget].asInstanceOf[Codec[T]]
  }

  implicit def requestChannelCodec[T <: RequestChannel]: Codec[T] = {
    @silent implicit val helloAllCodec: Codec[HelloAll]   = deriveCodecForUnaryCaseClass[HelloAll]
    @silent implicit val squareAllCodec: Codec[SquareAll] = deriveCodecForUnaryCaseClass[SquareAll]
    deriveCodec[RequestChannel].asInstanceOf[Codec[T]]
  }
}
