package msocket.api.models

import io.bullet.borer.derivation.CompactMapBasedCodecs
import io.bullet.borer.{Codec, Decoder, Encoder}

sealed trait Result[S, E] {
  def toEither: Either[E, S] = this match {
    case Result.Success(value) => Right(value)
    case Result.Error(value)   => Left(value)
  }
}

object Result {
  case class Success[S, E](value: S) extends Result[S, E]
  case class Error[S, E](value: E)   extends Result[S, E]

  def fromEither[E, S](either: Either[E, S]): Result[S, E] = either match {
    case Left(value)  => Error(value)
    case Right(value) => Success(value)
  }

  implicit def resultCodec[E: Encoder: Decoder, S: Encoder: Decoder]: Codec[Result[S, E]] = CompactMapBasedCodecs.deriveAllCodecs
}
