package msocket.api.codecs

import java.util.concurrent.TimeUnit

import akka.Done
import akka.util.Timeout
import io.bullet.borer.derivation.ArrayBasedCodecs
import io.bullet.borer.{Codec, Decoder, Encoder}
import msocket.api.models.Result

import scala.concurrent.duration.FiniteDuration

trait BasicCodecs {
  implicit def eitherEnc[E: Encoder, S: Encoder]: Encoder[Either[E, S]] =
    implicitly[Encoder[Result[S, E]]].contramap(Result.fromEither)

  implicit def eitherDec[E: Decoder, S: Decoder]: Decoder[Either[E, S]] =
    implicitly[Decoder[Result[S, E]]].map(_.toEither)

  implicit lazy val doneCodec: Codec[Done] = Codec.bimap[String, Done](_ => "done", _ => Done)

  implicit lazy val durationCodec: Codec[FiniteDuration] = Codec.bimap[(Long, String), FiniteDuration](
    finiteDuration => (finiteDuration.length, finiteDuration.unit.toString),
    { case (length, unitStr) => FiniteDuration(length, TimeUnit.valueOf(unitStr)) }
  )

  implicit lazy val timeoutCodec: Codec[Timeout] = ArrayBasedCodecs.deriveUnaryCodec

}
