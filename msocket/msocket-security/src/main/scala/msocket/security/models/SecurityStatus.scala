package msocket.security.models

sealed trait SecurityStatus

object SecurityStatus {
  sealed trait SecurityEnabled extends SecurityStatus

  case class TokenPresent(token: String) extends SecurityEnabled
  case object TokenMissing               extends SecurityEnabled
  case object Disabled                   extends SecurityStatus

  def from(maybeToken: Option[String], securityEnabled: Boolean): SecurityStatus = {
    (securityEnabled, maybeToken) match {
      case (false, _)          => SecurityStatus.Disabled
      case (true, Some(token)) => SecurityStatus.TokenPresent(token)
      case (true, None)        => SecurityStatus.TokenMissing
    }
  }
}
