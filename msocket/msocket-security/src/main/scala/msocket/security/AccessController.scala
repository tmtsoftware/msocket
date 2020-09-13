package msocket.security

import msocket.security.api.{AuthorizationPolicy, PassThroughPolicy, TokenValidator}
import msocket.security.models.{AccessStatus, AccessToken, SecurityStatus}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AccessController(tokenValidator: TokenValidator, securityStatus: SecurityStatus)(implicit ec: ExecutionContext) {

  def check(authorizationPolicy: AuthorizationPolicy): Future[AccessStatus] = {
    authorizationPolicy match {
      case PassThroughPolicy   => Future.successful(AccessStatus.Authorized(AccessToken.Empty))
      case authorizationPolicy =>
        securityStatus match {
          case SecurityStatus.Disabled            => Future.successful(AccessStatus.Authorized(AccessToken.Empty))
          case SecurityStatus.TokenMissing        => Future.successful(AccessStatus.AuthenticationFailed("access-token is missing"))
          case SecurityStatus.TokenPresent(token) =>
            tokenValidator.validate(token).flatMap { accessToken =>
              authorizationPolicy
                .authorize(accessToken)
                .map { isAuthorized =>
                  if (isAuthorized) AccessStatus.Authorized(accessToken) else AccessStatus.AuthorizationFailed("not enough access rights")
                }
                .recover {
                  case NonFatal(ex) => AccessStatus.AuthorizationFailed(ex.getMessage)
                }
            } recover {
              case NonFatal(ex) => AccessStatus.AuthenticationFailed(ex.getMessage)
            }
        }
    }
  }
}
