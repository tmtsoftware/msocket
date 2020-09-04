package msocket.api.security

sealed trait AccessStatus

object AccessStatus {
  case object Authorized           extends AccessStatus
  case object AuthenticationFailed extends AccessStatus
  case object AuthorizationFailed  extends AccessStatus
}
