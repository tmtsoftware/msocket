package msocket.http

import java.io.{StringWriter, Writer}
import java.util

import org.apache.pekko.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import io.prometheus.client.Collector.MetricFamilySamples
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat

case class MetricFamilySamplesEntity(samples: util.Enumeration[MetricFamilySamples])

object MetricFamilySamplesEntity {
  private val mediaTypeParams = Map("version" -> "0.0.4")
  private val mediaType       = MediaType.customWithFixedCharset("text", "plain", HttpCharsets.`UTF-8`, params = mediaTypeParams)

  def fromRegistry(collectorRegistry: CollectorRegistry): MetricFamilySamplesEntity =
    MetricFamilySamplesEntity(collectorRegistry.metricFamilySamples())

  def toPrometheusTextFormat(e: MetricFamilySamplesEntity): String = {
    val writer: Writer = new StringWriter()
    TextFormat.write004(writer, e.samples)
    writer.toString
  }

  implicit val metricsFamilySamplesMarshaller: ToEntityMarshaller[MetricFamilySamplesEntity] =
    Marshaller.withFixedContentType(mediaType) { s => HttpEntity(mediaType, toPrometheusTextFormat(s)) }
}

class MetricsEndpoint(registry: CollectorRegistry) {
  val routes: Route =
    get {
      path("metrics") {
        complete { MetricFamilySamplesEntity.fromRegistry(registry) }
      }
    }
}
