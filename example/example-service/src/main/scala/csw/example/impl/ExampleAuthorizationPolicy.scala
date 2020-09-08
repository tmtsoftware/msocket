package csw.example.impl

import msocket.security.api.AuthorizationPolicy
import msocket.security.models.AccessToken

import scala.concurrent.Future
import scala.util.Try

object ExampleAuthorizationPolicy {
  case object AuthenticatedPolicy extends AuthorizationPolicy {
    override def authorize(accessToken: AccessToken): Future[Boolean] = Future.successful(true)
  }

  case class AuthorizedPolicy(role: String) extends AuthorizationPolicy {
    override def authorize(accessToken: AccessToken): Future[Boolean] = Future.fromTry(Try(accessToken.hasRealmRole(role)))
  }
}
