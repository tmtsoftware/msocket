package msocket.security.models

sealed trait AccessStatus

object AccessStatus {
  case class Authorized(accessToken: AccessToken) extends AccessStatus

  sealed abstract class FailedAccessStatus(msg: String) extends RuntimeException(msg) with AccessStatus
  case class AuthenticationFailed(msg: String)          extends FailedAccessStatus(msg)
  case class AuthorizationFailed(msg: String)           extends FailedAccessStatus(msg)
}
