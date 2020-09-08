package msocket.jvm.mono

import io.bullet.borer.Encoder
import msocket.security.api.{AuthorizationPolicy, PassThroughPolicy}

import scala.concurrent.Future

trait MonoRequestHandler[Req] {
  def handle(request: Req): Future[MonoResponse]

  protected def future[Res: Encoder](
      result: => Future[Res],
      policy: AuthorizationPolicy = PassThroughPolicy
  ): Future[MonoResponse] = {
    Future.successful {
      new MonoResponse {
        override type Response = Res
        override def response: Future[Res]                    = result
        override def encoder: Encoder[Response]               = Encoder[Res]
        override def authorizationPolicy: AuthorizationPolicy = policy
      }
    }
  }
}

trait MonoResponse {
  type Response
  def response: Future[Response]
  def encoder: Encoder[Response]
  def authorizationPolicy: AuthorizationPolicy
}
