package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.{ErrorProtocol, Labellable}
import msocket.impl.RouteFactory1
import msocket.impl.metrics.Metrics
import msocket.impl.post.PostDirectives.withMetrics

import scala.collection.JavaConverters.asScalaIteratorConverter

class PostRouteFactory1[Req: Decoder: ErrorProtocol](endpoint: String, postHandler: HttpPostHandler[Req])
    extends RouteFactory1[Req]
    with ServerHttpCodecs {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(labelNames: List[String] = List.empty)(implicit labels: Req => Labellable[Req]): Route = {
    val httpCounter = Metrics.httpCounter(labelNames)

    println("labelNames : " + labelNames)
    post {
      path(endpoint) {
        PostDirectives.withAcceptHeader {
          withExceptionHandler {
            val route = withMetrics(httpCounter)(postHandler.handle)

            Metrics.prometheusRegistry.metricFamilySamples().asIterator().asScala.foreach(println)

            route
          }
        }
      }
    }
  }

}
