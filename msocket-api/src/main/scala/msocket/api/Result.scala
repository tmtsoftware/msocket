package msocket.api

import com.github.ghik.silencer.silent
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveUnaryCodec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec
import io.bullet.borer.{Codec, Decoder, Encoder}
import Result.{Error, Success}

sealed trait Result[S, E] {
  def toEither: Either[E, S] = this match {
    case Success(value) => Right(value)
    case Error(value)   => Left(value)
  }
}

object Result {
  case class Success[S, E](value: S) extends Result[S, E]
  case class Error[S, E](value: E)   extends Result[S, E]

  def fromEither[E, S](either: Either[E, S]): Result[S, E] = either match {
    case Left(value)  => Error(value)
    case Right(value) => Success(value)
  }

  implicit def resultCodec[E: Encoder: Decoder, S: Encoder: Decoder]: Codec[Result[S, E]] = {
    @silent implicit lazy val errorCodec: Codec[Error[S, E]]     = deriveUnaryCodec[Error[S, E]]
    @silent implicit lazy val successCodec: Codec[Success[S, E]] = deriveUnaryCodec[Success[S, E]]
    deriveCodec[Result[S, E]]
  }
}
