package msocket.api

import io.bullet.borer.{Codec, Decoder, Encoder}

trait EitherCodecs {
  implicit def eitherCodec[E: Encoder: Decoder, S: Encoder: Decoder]: Codec[Either[E, S]] = {
    Codec.bimap[Result[S, E], Either[E, S]](Result.fromEither, _.toEither)
  }

  implicit def eitherEnc[E: Encoder: Decoder, S: Encoder: Decoder]: Encoder[Either[E, S]] = eitherCodec[E, S].encoder
  implicit def eitherDec[E: Encoder: Decoder, S: Encoder: Decoder]: Decoder[Either[E, S]] = eitherCodec[E, S].decoder
}
