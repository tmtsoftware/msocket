package msocket.api

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.security.api.AuthorizationPolicy
import msocket.security.api.AsyncAuthorizationPolicy

import scala.concurrent.Future

trait StreamRequestHandler[Req] {
  def handle(request: Req): Future[StreamResponse]

  protected def future[Res: Encoder](
      result: => Future[Res],
      policy: AsyncAuthorizationPolicy = AuthorizationPolicy.PassThroughPolicy
  ): Future[StreamResponse] = {
    stream(Source.future(result), policy)
  }

  protected def stream[Res: Encoder](
      stream: => Source[Res, Any],
      policy: AsyncAuthorizationPolicy = AuthorizationPolicy.PassThroughPolicy
  ): Future[StreamResponse] = {
    Future.successful {
      new StreamResponse {
        override type Response = Res
        override def responseStream: Source[Response, Any]         = stream
        override def encoder: Encoder[Response]                    = Encoder[Res]
        override def authorizationPolicy: AsyncAuthorizationPolicy = policy
      }
    }
  }

}

trait StreamResponse {
  type Response
  def responseStream: Source[Response, Any]
  def encoder: Encoder[Response]
  def authorizationPolicy: AsyncAuthorizationPolicy
}
