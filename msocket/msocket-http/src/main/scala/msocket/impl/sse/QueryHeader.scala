package msocket.impl.sse

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

final case class QueryHeader(value: String) extends ModeledCustomHeader[QueryHeader] {
  override def companion: ModeledCustomHeaderCompanion[QueryHeader] = QueryHeader
  override def renderInRequests: Boolean                            = true
  override def renderInResponses: Boolean                           = true
}

object QueryHeader extends ModeledCustomHeaderCompanion[QueryHeader] {
  override def name: String                           = "Query"
  override def parse(value: String): Try[QueryHeader] = Try(new QueryHeader(value))
}
