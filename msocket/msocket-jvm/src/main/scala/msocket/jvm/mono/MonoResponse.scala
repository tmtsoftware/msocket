package msocket.jvm.mono

import io.bullet.borer.Encoder
import msocket.security.api.AuthorizationPolicy
import msocket.security.models.AccessToken

import scala.concurrent.Future

trait MonoResponse {
  type Response
  def responseFactory(accessToken: AccessToken): Future[Response]
  def encoder: Encoder[Response]
  def authorizationPolicy: AuthorizationPolicy
}

object MonoResponse {
  def from[Res: Encoder](resultFactory: AccessToken => Future[Res], policy: AuthorizationPolicy): MonoResponse = {
    new MonoResponse {
      override type Response = Res
      override def responseFactory(accessToken: AccessToken): Future[Response] = resultFactory(accessToken)
      override def encoder: Encoder[Response]                                  = Encoder[Res]
      override def authorizationPolicy: AuthorizationPolicy                    = policy
    }
  }
}
