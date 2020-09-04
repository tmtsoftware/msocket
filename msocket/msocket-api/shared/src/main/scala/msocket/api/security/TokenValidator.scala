package msocket.api.security

import scala.concurrent.Future

trait TokenValidator {
  def validate(token: String): Future[AccessToken]
}
