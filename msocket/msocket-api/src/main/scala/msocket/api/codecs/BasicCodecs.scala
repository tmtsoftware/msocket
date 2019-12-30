package msocket.api.codecs

import java.util.concurrent.TimeUnit

import akka.Done
import akka.util.Timeout
import com.github.ghik.silencer.silent
import io.bullet.borer.derivation.ArrayBasedCodecs
import io.bullet.borer.{Codec, Decoder, Encoder}
import msocket.api.models.Result

import scala.concurrent.duration.FiniteDuration

object BasicCodecs extends BasicCodecs
trait BasicCodecs {
  implicit def eitherEnc[E: Encoder, S: Encoder]: Encoder[Either[E, S]] = Encoder[Result[S, E]].contramap(Result.fromEither)
  implicit def eitherDec[E: Decoder, S: Decoder]: Decoder[Either[E, S]] = Decoder[Result[S, E]].map(_.toEither)

  implicit lazy val doneCodec: Codec[Done] = Codec.bimap[String, Done](_ => "done", _ => Done)

  implicit lazy val timeoutInSecondsCodec: Codec[Timeout] = {
    @silent implicit lazy val durationInSecondsCodec: Codec[FiniteDuration] =
      Codec.bimap[Long, FiniteDuration](_.toSeconds, FiniteDuration(_, TimeUnit.SECONDS))

    ArrayBasedCodecs.deriveUnaryCodec
  }
}
