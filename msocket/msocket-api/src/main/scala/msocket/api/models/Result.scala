package msocket.api.models

import io.bullet.borer.derivation.CompactMapBasedCodecs
import io.bullet.borer.{Codec, Decoder, Encoder}

sealed trait Result[S, E]

object Result {
  case class success[S, E](value: S) extends Result[S, E]
  case class error[S, E](value: E)   extends Result[S, E]

  implicit def resultCodec[E: Encoder: Decoder, S: Encoder: Decoder]: Codec[Result[S, E]] = CompactMapBasedCodecs.deriveAllCodecs

  def fromEither[S, E](either: Either[E, S]): Result[S, E] =
    either match {
      case Left(value)  => error(value)
      case Right(value) => success(value)
    }

  def toEither[S, E](result: Result[S, E]): Either[E, S] =
    result match {
      case success(value) => Right(value)
      case error(value)   => Left(value)
    }
}
