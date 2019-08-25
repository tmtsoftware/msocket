package mscoket.impl

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{Payload, Result}
import msocket.api.Result.{Error, Success}

import scala.concurrent.{ExecutionContext, Future}

object ToPayload {
  implicit class FutureToPayload[T](future: Future[T]) {
    def payload(implicit ec: ExecutionContext, encoder: Encoder[T]): Source[Payload[T], NotUsed] = {
      Source.fromFuture(future.map(x => Payload(x)))
    }
  }

  implicit class SourceToPayload[T, Mat](stream: Source[T, Mat]) {
    def payloads(implicit encoder: Encoder[T]): Source[Payload[T], Mat] = {
      stream.map(x => Payload(x))
    }
  }

  implicit class SourceWithErrorToPayload[S, E](stream: Source[S, Future[Option[E]]]) {
    def resultPayloads(
        implicit encS: Encoder[S],
        encE: Encoder[E],
        decS: Decoder[S],
        decE: Decoder[E],
        mat: Materializer
    ): Source[Payload[Result[S, E]], NotUsed] = {
      val (matF, source) = stream.preMaterialize()
      val resultStream: Source[Result[S, E], NotUsed] = Source.fromFuture(matF).flatMapConcat {
        case Some(value) => Source.single(Error(value))
        case None        => source.map(Success(_))
      }
      resultStream.payloads
    }
  }
}
