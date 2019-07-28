package msocket.core.extensions

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.core.api.Payload

import scala.concurrent.{ExecutionContext, Future}

object ToResponse {
  implicit class FutureToPayload[T](future: Future[T]) {
    def response(implicit ec: ExecutionContext, encoder: Encoder[T]): Future[Payload[T]] = future.map(x => Payload(x))
  }

  implicit class SourceToPayload[T, Mat](stream: Source[T, Mat]) {
    def responses(implicit encoder: Encoder[T]): Source[Payload[T], Mat] = stream.map(x => Payload(x))
  }
}
