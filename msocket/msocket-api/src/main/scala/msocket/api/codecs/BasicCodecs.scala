package msocket.api.codecs

import akka.Done
import io.bullet.borer.{Codec, Decoder, Encoder}
import msocket.api.models.Result

trait BasicCodecs {
  implicit def eitherEnc[E: Encoder, S: Encoder]: Encoder[Either[E, S]] =
    implicitly[Encoder[Result[S, E]]].contramap(Result.fromEither)

  implicit def eitherDec[E: Decoder, S: Decoder]: Decoder[Either[E, S]] =
    implicitly[Decoder[Result[S, E]]].map(_.toEither)

  implicit lazy val doneCodec: Codec[Done] = Codec.bimap[String, Done](_ => "done", _ => Done)
}
