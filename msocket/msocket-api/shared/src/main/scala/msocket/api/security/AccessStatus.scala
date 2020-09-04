package msocket.api.security

sealed trait AccessStatus

object AccessStatus {
  case object Authorized extends AccessStatus

  sealed abstract class FailedAccessStatus(msg: String) extends RuntimeException(msg) with AccessStatus
  case class AuthenticationFailed(msg: String)          extends FailedAccessStatus(msg)
  case class AuthorizationFailed(msg: String)           extends FailedAccessStatus(msg)
}
