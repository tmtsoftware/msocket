package mscoket.impl.sse

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

final class QueryHeader(queryString: String) extends ModeledCustomHeader[QueryHeader] {
  override def companion: ModeledCustomHeaderCompanion[QueryHeader] = QueryHeader
  override def value: String                                        = queryString
  override def renderInRequests: Boolean                            = true
  override def renderInResponses: Boolean                           = true
}

object QueryHeader extends ModeledCustomHeaderCompanion[QueryHeader] {
  override def name: String                           = "query"
  override def parse(value: String): Try[QueryHeader] = Try(new QueryHeader(value))
}
