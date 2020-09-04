package msocket.security.api

import scala.concurrent.Future

trait TokenValidator {
  def validate(token: String): Future[AccessToken]
}
