package msocket.api.security

import scala.concurrent.Future

trait AsyncAuthorizationPolicy {
  def asyncAuthorize(accessToken: AccessToken): Future[Boolean]
}
