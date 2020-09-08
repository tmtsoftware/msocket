package msocket.jvm.stream

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.security.api.{AuthorizationPolicy, PassThroughPolicy}

import scala.concurrent.Future

trait StreamRequestHandler[Req] {
  def handle(request: Req): Future[StreamResponse]

  protected def future[Res: Encoder](
      result: => Future[Res],
      policy: AuthorizationPolicy = PassThroughPolicy
  ): Future[StreamResponse] = {
    stream(Source.future(result), policy)
  }

  protected def stream[Res: Encoder](
      stream: => Source[Res, Any],
      policy: AuthorizationPolicy = PassThroughPolicy
  ): Future[StreamResponse] = {
    Future.successful {
      new StreamResponse {
        override type Response = Res
        override def responseStream: Source[Response, Any]    = stream
        override def encoder: Encoder[Response]               = Encoder[Res]
        override def authorizationPolicy: AuthorizationPolicy = policy
      }
    }
  }

}

trait StreamResponse {
  type Response
  def responseStream: Source[Response, Any]
  def encoder: Encoder[Response]
  def authorizationPolicy: AuthorizationPolicy
}
