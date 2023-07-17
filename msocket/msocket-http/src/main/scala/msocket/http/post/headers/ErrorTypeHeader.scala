package msocket.http.post.headers

import org.apache.pekko.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

final case class ErrorTypeHeader(value: String) extends ModeledCustomHeader[ErrorTypeHeader] {
  override def companion: ModeledCustomHeaderCompanion[ErrorTypeHeader] = ErrorTypeHeader
  override def renderInRequests: Boolean                                = true
  override def renderInResponses: Boolean                               = true
}

object ErrorTypeHeader extends ModeledCustomHeaderCompanion[ErrorTypeHeader] {
  override def name: String                               = "Error-Type"
  override def parse(value: String): Try[ErrorTypeHeader] = Try(new ErrorTypeHeader(value))
}
