package msocket.core.extensions

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.core.api.Response

import scala.concurrent.{ExecutionContext, Future}

object ToResponse {
  implicit class FutureToPayload[T](future: Future[T]) {
    def response(implicit ec: ExecutionContext, encoder: Encoder[T]): Future[Response[T]] = future.map(x => Response(x))
  }

  implicit class SourceToPayload[T, Mat](stream: Source[T, Mat]) {
    def responses(implicit encoder: Encoder[T]): Source[Response[T], Mat] = stream.map(x => Response(x))
  }
}
