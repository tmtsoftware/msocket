package mscoket.impl

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.Result
import msocket.api.Result.{Error, Success}

import scala.concurrent.Future

trait StreamExtensions[M] {
  def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[M, Mat]

  def stream[T](input: Future[T])(implicit encoder: Encoder[T]): Source[M, NotUsed] = {
    stream(Source.fromFuture(input))
  }

  def streamWithError[S, E](input: Source[S, Future[Option[E]]])(
      implicit encS: Encoder[S],
      encE: Encoder[E],
      mat: Materializer
  ): Source[M, NotUsed] = {
    val (matF, source) = input.preMaterialize()
    val resultStream: Source[Result[S, E], NotUsed] = Source.fromFuture(matF).flatMapConcat {
      case Some(value) => Source.single(Error(value))
      case None        => source.map(Success(_))
    }
    stream(resultStream)
  }
}
