package msocket.api

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder

import scala.concurrent.Future

trait StreamRequestHandler[Req] {
  def handle(request: Req): Future[StreamResponse]

  protected def future[Res: Encoder](result: Future[Res]): Future[StreamResponse] = {
    stream(Source.future(result))
  }

  protected def stream[Res: Encoder](stream: Source[Res, Any]): Future[StreamResponse] = {
    Future.successful {
      new StreamResponse {
        override type Response = Res
        override def responseStream: Source[Response, Any] = stream
        override def encoder: Encoder[Response]            = Encoder[Res]
      }
    }
  }

}

trait StreamResponse {
  type Response
  def responseStream: Source[Response, Any]
  def encoder: Encoder[Response]
}
