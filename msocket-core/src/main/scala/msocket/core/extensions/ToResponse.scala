package msocket.core.extensions

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.core.api.MResponse

import scala.concurrent.{ExecutionContext, Future}

object ToResponse {
  implicit class FutureToPayload[T](future: Future[T]) {
    def response(implicit ec: ExecutionContext, encoder: Encoder[T]): Future[MResponse[T]] = future.map(x => MResponse(x))
  }

  implicit class SourceToPayload[T, Mat](stream: Source[T, Mat]) {
    def responses(implicit encoder: Encoder[T]): Source[MResponse[T], Mat] = stream.map(x => MResponse(x))
  }
}
