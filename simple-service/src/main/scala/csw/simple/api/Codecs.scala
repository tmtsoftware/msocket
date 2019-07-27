package csw.simple.api

import akka.Done
import com.github.ghik.silencer.silent
import csw.simple.api.Protocol._
import io.bullet.borer.derivation.MapBasedCodecs._
import io.bullet.borer.{Codec, Json, Target}

trait Codecs {
  implicit val target: Target = Json

  implicit lazy val doneCodec: Codec[Done] = Codec.implicitly[String].bimap[Done](_ => "done", _ => Done)

  implicit def protocolCodec[T <: Protocol]: Codec[T] = explicitProtocolCodec.asInstanceOf[Codec[T]]

  lazy val explicitProtocolCodec: Codec[Protocol] = {
    @silent implicit lazy val helloCodec: Codec[Hello]   = deriveCodec[Hello]
    @silent implicit lazy val squareCodec: Codec[Square] = deriveCodec[Square]

    @silent implicit val getNamesCodec: Codec[GetNames]     = deriveCodec[GetNames]
    @silent implicit val getNumbersCodec: Codec[GetNumbers] = deriveCodec[GetNumbers]

    @silent implicit val pingCodec: Codec[Ping]       = deriveCodec[Ping]
    @silent implicit val publishCodec: Codec[Publish] = deriveCodec[Publish]

    @silent implicit val helloAllCodec: Codec[HelloAll]   = deriveCodec[HelloAll]
    @silent implicit val squareAllCodec: Codec[SquareAll] = deriveCodec[SquareAll]

    deriveCodec[Protocol]
  }
}
