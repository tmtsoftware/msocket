package msocket.jvm.stream

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.security.api.AuthorizationPolicy

trait StreamResponse {
  type Response
  def responseStream: Source[Response, Any]
  def encoder: Encoder[Response]
  def authorizationPolicy: AuthorizationPolicy
}

object StreamResponse {
  def from[Res: Encoder](stream: => Source[Res, Any], policy: AuthorizationPolicy): StreamResponse = {
    new StreamResponse {
      override type Response = Res
      override lazy val responseStream: Source[Response, Any] = stream
      override lazy val encoder: Encoder[Response]            = Encoder[Res]
      override def authorizationPolicy: AuthorizationPolicy   = policy
    }
  }
}
