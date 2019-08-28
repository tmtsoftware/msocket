package mscoket.impl.sse

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

final class PayloadHeader(data: String) extends ModeledCustomHeader[PayloadHeader] {
  override def companion: ModeledCustomHeaderCompanion[PayloadHeader] = PayloadHeader
  override def value: String                                          = data
  override def renderInRequests: Boolean                              = true
  override def renderInResponses: Boolean                             = true
}

object PayloadHeader extends ModeledCustomHeaderCompanion[PayloadHeader] {
  override def name: String                             = "payload"
  override def parse(value: String): Try[PayloadHeader] = Try(new PayloadHeader(value))
}
