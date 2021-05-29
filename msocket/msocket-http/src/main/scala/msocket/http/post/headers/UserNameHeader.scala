package msocket.http.post.headers

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

final case class UserNameHeader(value: String) extends ModeledCustomHeader[UserNameHeader] {
  override def companion: ModeledCustomHeaderCompanion[UserNameHeader] = UserNameHeader
  override def renderInRequests: Boolean                               = true
  override def renderInResponses: Boolean                              = true
}

object UserNameHeader extends ModeledCustomHeaderCompanion[UserNameHeader] {
  override def name: String                              = "Username"
  override def parse(value: String): Try[UserNameHeader] = Try(new UserNameHeader(value))
}
