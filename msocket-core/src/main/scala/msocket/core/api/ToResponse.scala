package msocket.core.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder

import scala.concurrent.{ExecutionContext, Future}

object ToResponse {
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
}
