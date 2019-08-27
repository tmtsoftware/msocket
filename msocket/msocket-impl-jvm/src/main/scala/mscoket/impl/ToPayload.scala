package mscoket.impl

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import mscoket.impl.Encoding.JsonText
import msocket.api.Result
import msocket.api.Result.{Error, Success}

import scala.concurrent.{ExecutionContext, Future}

object ToPayload {
  implicit class FutureToPayload[T](future: Future[T]) {
    def payload(implicit ec: ExecutionContext, encoder: Encoder[T]): Source[Message, NotUsed] = {
      Source.fromFuture(future.map(JsonText.strictMessage[T]))
    }
  }

  implicit class SourceToPayload[T, Mat](stream: Source[T, Mat]) {
    def payloads(implicit encoder: Encoder[T]): Source[Message, Mat] = {
      stream.map(JsonText.strictMessage[T])
    }
  }

  implicit class SourceWithErrorToPayload[S, E](stream: Source[S, Future[Option[E]]]) {
    def resultPayloads(
        implicit encS: Encoder[S],
        encE: Encoder[E],
        mat: Materializer
    ): Source[Message, NotUsed] = {
      val (matF, source) = stream.preMaterialize()
      val resultStream: Source[Result[S, E], NotUsed] = Source.fromFuture(matF).flatMapConcat {
        case Some(value) => Source.single(Error(value))
        case None        => source.map(Success(_))
      }
      resultStream.payloads
    }
  }
}
