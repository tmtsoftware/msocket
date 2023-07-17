package msocket.api.codecs

import java.util.concurrent.TimeUnit

import org.apache.pekko.Done
import org.apache.pekko.util.Timeout
import io.bullet.borer.derivation.CompactMapBasedCodecs
import io.bullet.borer.{Codec, Decoder, Encoder}
import msocket.api.models.Result

import scala.annotation.nowarn
import scala.concurrent.duration.FiniteDuration

object BasicCodecs extends BasicCodecs
trait BasicCodecs {
  implicit def eitherCodec[E: Encoder: Decoder, S: Encoder: Decoder]: Codec[Either[E, S]] =
    Codec.of[Result[S, E]].bimap(Result.fromEither, Result.toEither)

  implicit lazy val doneCodec: Codec[Done] = Codec.bimap[String, Done](_ => "Done", _ => Done)

  implicit lazy val timeoutInSecondsCodec: Codec[Timeout] = {
    @nowarn implicit val durationInSecondsCodec: Codec[FiniteDuration] =
      Codec.bimap[Long, FiniteDuration](_.toSeconds, FiniteDuration(_, TimeUnit.SECONDS))

    CompactMapBasedCodecs.deriveCodec
  }

}
