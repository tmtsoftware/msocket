package msocket.security

import msocket.security.api.{AuthorizationPolicy, TokenValidator}
import msocket.security.models._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AccessController(tokenValidator: TokenValidator, securityStatus: SecurityStatus)(implicit ec: ExecutionContext) {

  def authenticateAndAuthorize(authorizationPolicy: Option[AuthorizationPolicy]): Future[AccessStatus] = {
    authorizationPolicy match {
      case None         => Future.successful(AccessStatus.Authorized(AccessToken.Empty))
      case Some(policy) =>
        securityStatus match {
          case SecurityStatus.Disabled            => Future.successful(AccessStatus.Authorized(AccessToken.Empty))
          case SecurityStatus.TokenMissing        => Future.successful(AccessStatus.TokenMissing())
          case SecurityStatus.TokenPresent(token) =>
            tokenValidator
              .validate(token)
              .flatMap { accessToken =>
                policy
                  .authorize(accessToken)
                  .map { isAuthorized =>
                    if (isAuthorized) AccessStatus.Authorized(accessToken)
                    else AccessStatus.AuthorizationFailed("not enough access rights")
                  }
                  .recover {
                    case NonFatal(ex) => AccessStatus.AuthorizationFailed(ex.getMessage)
                  }
              }
              .recover {
                case NonFatal(ex) => AccessStatus.AuthenticationFailed(ex.getMessage)
              }
        }
    }
  }

}
