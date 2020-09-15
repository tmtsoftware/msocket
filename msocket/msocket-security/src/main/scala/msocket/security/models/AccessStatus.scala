package msocket.security.models

sealed trait AccessStatus

object AccessStatus {
  case class Authorized(accessToken: AccessToken) extends AccessStatus
  case class TokenMissing()                       extends RuntimeException("token is missing") with AccessStatus
  case class AuthenticationFailed(msg: String)    extends RuntimeException(msg) with AccessStatus
  case class AuthorizationFailed(msg: String)     extends RuntimeException(msg) with AccessStatus
}
