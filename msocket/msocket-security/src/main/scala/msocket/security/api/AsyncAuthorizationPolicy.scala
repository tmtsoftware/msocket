package msocket.security.api

import msocket.security.models.AccessToken

import scala.concurrent.Future

trait AsyncAuthorizationPolicy {
  def asyncAuthorize(accessToken: AccessToken): Future[Boolean]
}
