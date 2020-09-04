package msocket.api.security

trait TokenValidator {
  def validate(token: String): Option[AccessToken]
}
