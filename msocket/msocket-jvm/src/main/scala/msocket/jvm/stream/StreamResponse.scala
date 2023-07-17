package msocket.jvm.stream

import org.apache.pekko.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.security.api.AuthorizationPolicy
import msocket.security.models.AccessToken

trait StreamResponse {
  type Response
  def responseFactory(accessToken: AccessToken): Source[Response, Any]
  def encoder: Encoder[Response]
  def authorizationPolicy: Option[AuthorizationPolicy]
}

object StreamResponse {
  def from[Res: Encoder](streamFactory: AccessToken => Source[Res, Any], policy: Option[AuthorizationPolicy]): StreamResponse = {
    new StreamResponse {
      override type Response = Res
      override def responseFactory(accessToken: AccessToken): Source[Res, Any] = streamFactory(accessToken)
      override def encoder: Encoder[Response]                                  = Encoder[Res]
      override def authorizationPolicy: Option[AuthorizationPolicy]            = policy
    }
  }
}
