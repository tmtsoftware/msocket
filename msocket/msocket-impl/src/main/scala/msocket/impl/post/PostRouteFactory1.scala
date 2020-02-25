package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, Labelled}
import msocket.impl.RouteFactory1
import msocket.impl.metrics.Metrics
import msocket.impl.post.PostDirectives.withMetrics

import scala.jdk.CollectionConverters._

class PostRouteFactory1[Req: Decoder: ErrorProtocol](endpoint: String, postHandler: HttpPostHandler[Req])
    extends RouteFactory1[Req]
    with ServerHttpCodecs {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(labelNames: List[String] = List.empty, metricsEnabled: Boolean = false)(implicit labelGen: Req => Labelled[Req]): Route = {
    lazy val httpCounter = Metrics.httpCounter(labelNames)

    post {
      path(endpoint) {
        PostDirectives.withAcceptHeader {
          withExceptionHandler {
            val route =
              if (metricsEnabled) withMetrics(httpCounter)(postHandler.handle)
              else entity(as[Req])(postHandler.handle)

            Metrics.prometheusRegistry.metricFamilySamples().asIterator().asScala.foreach(println)
            route
          }
        }
      }
    }
  }

}
