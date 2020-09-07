package msocket.security

import msocket.security.api.TokenValidator
import msocket.security.models.SecurityStatus

import scala.concurrent.{ExecutionContext, Future}

class AccessControllerFactory(tokenValidator: TokenValidator, securityEnabled: Boolean)(implicit ec: ExecutionContext) {
  def make(token: Option[String]) = new AccessController(tokenValidator, SecurityStatus.from(token, securityEnabled))
}

object AccessControllerFactory {
  def noop(implicit ec: ExecutionContext) =
    new AccessControllerFactory(
      tokenValidator = _ => Future.failed(new RuntimeException("access control is not supported for streaming transport, yet!")),
      securityEnabled = false
    )
}
