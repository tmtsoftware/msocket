package msocket.js.metrics

import java.io.StringWriter

import org.apache.pekko.http.scaladsl.model.HttpCharsets
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.{CollectorRegistry, Histogram}
import msocket.http.MetricsEndpoint
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class MetricsEndpointTest extends AnyWordSpec with Matchers with ScalatestRouteTest {
  private val RandomTestName = generateRandomStringOfLength(16)
  private val RandomTestHelp = generateRandomStringOfLength(32)

  "Metrics endpoint" must {
    "return the correct media type and charset" in {
      val api = new MetricsEndpoint(CollectorRegistry.defaultRegistry)
      Get("/metrics") ~> api.routes ~> check {
        mediaType.subType shouldBe "plain"
        mediaType.isText shouldBe true
        mediaType.params shouldBe Map("version" -> "0.0.4")
        charset shouldBe HttpCharsets.`UTF-8`
      }
    }

    "return serialized metrics in the prometheus text format" in {
      val registry = CollectorRegistry.defaultRegistry
      val api      = new MetricsEndpoint(CollectorRegistry.defaultRegistry)
      val hist     = Histogram.build().name(RandomTestName).help(RandomTestHelp).linearBuckets(0, 1, 10).register(registry)

      hist.observe(Math.abs(Random.nextDouble()))

      Get("/metrics") ~> api.routes ~> check {
        val resp   = responseAs[String]
        val writer = new StringWriter()
        TextFormat.write004(writer, registry.metricFamilySamples())

        resp shouldBe writer.toString
      }
    }
  }

  private def generateRandomStringOfLength(length: Int): String = Random.alphanumeric.filter(_.isLetter).take(length).mkString("")
}
