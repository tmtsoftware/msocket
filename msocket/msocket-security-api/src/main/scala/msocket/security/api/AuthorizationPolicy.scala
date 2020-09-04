package msocket.security.api

import scala.concurrent.Future
import scala.util.control.NonFatal

trait AuthorizationPolicy extends AsyncAuthorizationPolicy {
  protected def authorize(accessToken: AccessToken): Boolean

  final override def asyncAuthorize(accessToken: AccessToken): Future[Boolean] =
    try {
      Future.successful(authorize(accessToken))
    } catch {
      case NonFatal(ex) => Future.failed(ex)
    }
}

object AuthorizationPolicy {
  case object PassThroughPolicy extends AuthorizationPolicy {
    override def authorize(accessToken: AccessToken): Boolean = {
      throw new RuntimeException(s"authorization should not be checked for $PassThroughPolicy policy")
    }
  }

  case object AuthenticatedPolicy extends AuthorizationPolicy {
    override protected def authorize(accessToken: AccessToken): Boolean = true
  }

  case class AuthorizedPolicy(role: String) extends AuthorizationPolicy {
    override protected def authorize(accessToken: AccessToken): Boolean = accessToken.hasRealmRole(role)
  }

}
