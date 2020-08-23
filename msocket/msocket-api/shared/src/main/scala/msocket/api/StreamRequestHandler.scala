package msocket.api

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder

import scala.concurrent.Future

trait StreamRequestHandler[Req] {
  def handle(request: Req): StreamResponse

  protected def future[Res: Encoder](futureElement: Future[Res]): StreamResponse = {
    stream(Source.future(futureElement))
  }

  protected def stream[Res: Encoder](stream: Source[Res, Any]): StreamResponse = {
    new StreamResponse {
      override type Response = Res
      override def responseStream: Source[Response, Any] = stream
      override def encoder: Encoder[Response]            = Encoder[Res]
    }
  }

}

trait StreamResponse {
  type Response
  def responseStream: Source[Response, Any]
  def encoder: Encoder[Response]
}
