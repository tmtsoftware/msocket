package msocket.security.api

import scala.concurrent.Future

trait AsyncAuthorizationPolicy {
  def asyncAuthorize(accessToken: AccessToken): Future[Boolean]
}
