package msocket.security

import msocket.security.api.TokenValidator
import msocket.security.models.SecurityStatus

import scala.concurrent.{ExecutionContext, Future}

class AccessControllerFactory(tokenValidator: TokenValidator, securityEnabled: Boolean) {
  def make(token: Option[String])(implicit ec: ExecutionContext) =
    new AccessController(tokenValidator, SecurityStatus.from(token, securityEnabled))
}

object AccessControllerFactory {
  def noop =
    new AccessControllerFactory(
      tokenValidator = _ => Future.failed(new RuntimeException("access control is not supported for streaming transport, yet!")),
      securityEnabled = false
    )
}
