package msocket.jvm.stream

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.security.api.AuthorizationPolicy
import msocket.security.models.AccessToken

trait StreamResponse {
  type Response
  def responseFactory(accessToken: AccessToken): Source[Response, Any]
  def encoder: Encoder[Response]
  def authorizationPolicy: AuthorizationPolicy
}

object StreamResponse {
  def from[Res: Encoder](streamFactory: AccessToken => Source[Res, Any], policy: AuthorizationPolicy): StreamResponse = {
    new StreamResponse {
      override type Response = Res
      override def responseFactory(accessToken: AccessToken): Source[Res, Any] = streamFactory(accessToken)
      override def encoder: Encoder[Response]                                  = Encoder[Res]
      override def authorizationPolicy: AuthorizationPolicy                    = policy
    }
  }
}
