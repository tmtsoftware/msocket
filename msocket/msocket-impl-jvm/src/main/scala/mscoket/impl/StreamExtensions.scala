package mscoket.impl

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.utils.{Result, StreamError, StreamStatus, StreamSuccess}

import scala.concurrent.Future

trait StreamExtensions[M] {
  def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[M, Mat]

  def futureAsStream[T](input: Future[T])(implicit encoder: Encoder[T]): Source[M, NotUsed] = {
    stream(Source.fromFuture(input))
  }

  def streamWithError[S](input: Source[S, Future[StreamStatus]])(implicit encS: Encoder[S], mat: Materializer): Source[M, NotUsed] = {
    val (matF, source) = input.preMaterialize()
    val resultStream: Source[Result[S, StreamStatus], NotUsed] = Source.fromFuture(matF).flatMapConcat {
      case error: StreamError => Source.single(Result.Error(error))
      case StreamSuccess      => source.map(Result.Success(_))
    }
    stream(resultStream)
  }
}
