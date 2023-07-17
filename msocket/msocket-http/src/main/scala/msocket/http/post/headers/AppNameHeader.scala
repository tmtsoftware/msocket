package msocket.http.post.headers

import org.apache.pekko.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

final case class AppNameHeader(value: String) extends ModeledCustomHeader[AppNameHeader] {
  override def companion: ModeledCustomHeaderCompanion[AppNameHeader] = AppNameHeader
  override def renderInRequests: Boolean                              = true
  override def renderInResponses: Boolean                             = true
}

object AppNameHeader extends ModeledCustomHeaderCompanion[AppNameHeader] {
  override def name: String                             = "X-TMT-App-Name"
  override def parse(value: String): Try[AppNameHeader] = Try(new AppNameHeader(value))
}
