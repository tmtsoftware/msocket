package msocket.api.codecs

import akka.Done
import io.bullet.borer.{Codec, Decoder, Encoder}
import msocket.api.models.Result

object BasicCodecs extends BasicCodecs
trait BasicCodecs {
  implicit def eitherCodec[E: Encoder: Decoder, S: Encoder: Decoder]: Codec[Either[E, S]] =
    Codec.of[Result[S, E]].bimap(Result.fromEither, Result.toEither)

  implicit lazy val doneCodec: Codec[Done] = Codec.bimap[String, Done](_ => "done", _ => Done)
}
