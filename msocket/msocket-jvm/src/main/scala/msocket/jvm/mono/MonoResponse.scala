package msocket.jvm.mono

import io.bullet.borer.Encoder
import msocket.security.api.AuthorizationPolicy

import scala.concurrent.Future

trait MonoResponse {
  type Response
  def response: Future[Response]
  def encoder: Encoder[Response]
  def authorizationPolicy: AuthorizationPolicy
}

object MonoResponse {
  def from[Res: Encoder](result: => Future[Res], policy: AuthorizationPolicy): MonoResponse = {
    new MonoResponse {
      override type Response = Res
      override lazy val response: Future[Res]               = result
      override lazy val encoder: Encoder[Response]          = Encoder[Res]
      override def authorizationPolicy: AuthorizationPolicy = policy
    }
  }
}
