package msocket.api.security

import scala.concurrent.{ExecutionContext, Future}

class AccessController(tokenValidator: TokenValidator, securityStatus: SecurityStatus)(implicit ec: ExecutionContext) {

  def check(authorizationPolicy: AsyncAuthorizationPolicy): Future[AccessStatus] = {
    authorizationPolicy match {
      case AuthorizationPolicy.NotRequired => Future.successful(AccessStatus.Authorized)
      case authorizationPolicy             =>
        securityStatus match {
          case SecurityStatus.Disabled            => Future.successful(AccessStatus.Authorized)
          case SecurityStatus.TokenMissing        => Future.successful(AccessStatus.AuthenticationFailed)
          case SecurityStatus.TokenPresent(token) =>
            tokenValidator.validate(token) match {
              case None              => Future.successful(AccessStatus.AuthenticationFailed)
              case Some(accessToken) =>
                authorizationPolicy.asyncAuthorize(accessToken).map { isAuthorized =>
                  if (isAuthorized) AccessStatus.Authorized else AccessStatus.AuthorizationFailed
                }
            }
        }
    }
  }
}

class AccessControllerFactory(tokenValidator: TokenValidator, securityEnabled: Boolean)(implicit ec: ExecutionContext) {
  def make(token: Option[String]) = new AccessController(tokenValidator, SecurityStatus.from(token, securityEnabled))
}
