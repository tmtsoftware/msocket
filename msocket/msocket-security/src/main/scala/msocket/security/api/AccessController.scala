package msocket.security.api

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AccessController(tokenValidator: TokenValidator, securityStatus: SecurityStatus)(implicit ec: ExecutionContext) {

  def check(authorizationPolicy: AsyncAuthorizationPolicy): Future[AccessStatus] = {
    authorizationPolicy match {
      case AuthorizationPolicy.PassThroughPolicy => Future.successful(AccessStatus.Authorized)
      case authorizationPolicy                   =>
        securityStatus match {
          case SecurityStatus.Disabled            => Future.successful(AccessStatus.Authorized)
          case SecurityStatus.TokenMissing        => Future.successful(AccessStatus.AuthenticationFailed("access-token is missing"))
          case SecurityStatus.TokenPresent(token) =>
            tokenValidator.validate(token).flatMap { accessToken =>
              authorizationPolicy.asyncAuthorize(accessToken).map { isAuthorized =>
                if (isAuthorized) AccessStatus.Authorized else AccessStatus.AuthorizationFailed("not enough access rights")
              }
            } recover {
              case NonFatal(ex) => AccessStatus.AuthenticationFailed(ex.getMessage)
            }
        }
    }
  }
}
