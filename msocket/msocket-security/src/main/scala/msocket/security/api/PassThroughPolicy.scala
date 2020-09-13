package msocket.security.api

import msocket.security.models.AccessToken

import scala.concurrent.Future

private[msocket] case object PassThroughPolicy extends AuthorizationPolicy {
  override def authorize(accessToken: AccessToken): Future[Boolean] = {
    Future.failed(new RuntimeException(s"authorization should not be checked for $PassThroughPolicy policy"))
  }
}
