package msocket.security.api

import scala.concurrent.ExecutionContext

class AccessControllerFactory(tokenValidator: TokenValidator, securityEnabled: Boolean)(implicit ec: ExecutionContext) {
  def make(token: Option[String]) = new AccessController(tokenValidator, SecurityStatus.from(token, securityEnabled))
}
