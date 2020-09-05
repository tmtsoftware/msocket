package msocket.security.api

import msocket.security.models.AccessToken

import scala.concurrent.Future

trait TokenValidator {
  def validate(token: String): Future[AccessToken]
}
