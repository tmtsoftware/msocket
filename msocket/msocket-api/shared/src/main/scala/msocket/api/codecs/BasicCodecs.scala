package msocket.api.codecs

import java.util.concurrent.TimeUnit

import akka.Done
import akka.util.Timeout
import com.github.ghik.silencer.silent
import io.bullet.borer.derivation.CompactMapBasedCodecs
import io.bullet.borer.{Codec, Decoder, Encoder}
import msocket.api.models.Result

import scala.concurrent.duration.FiniteDuration

object BasicCodecs extends BasicCodecs
trait BasicCodecs {
  implicit def eitherCodec[E: Encoder: Decoder, S: Encoder: Decoder]: Codec[Either[E, S]] =
    Codec.of[Result[S, E]].bimap(Result.fromEither, Result.toEither)

  implicit lazy val doneCodec: Codec[Done] = Codec.bimap[String, Done](_ => "done", _ => Done)

  implicit lazy val timeoutInSecondsCodec: Codec[Timeout] = {
    @silent implicit val durationInSecondsCodec: Codec[FiniteDuration] =
      Codec.bimap[Long, FiniteDuration](_.toSeconds, FiniteDuration(_, TimeUnit.SECONDS))

    CompactMapBasedCodecs.deriveCodec
  }

}
