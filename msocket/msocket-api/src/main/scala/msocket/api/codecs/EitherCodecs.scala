package msocket.api.codecs

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.models.Result

trait EitherCodecs {
  implicit def eitherEnc[E: Encoder, S: Encoder]: Encoder[Either[E, S]] =
    implicitly[Encoder[Result[S, E]]].contramap(Result.fromEither)

  implicit def eitherDec[E: Decoder, S: Decoder]: Decoder[Either[E, S]] =
    implicitly[Decoder[Result[S, E]]].map(_.toEither)
}
