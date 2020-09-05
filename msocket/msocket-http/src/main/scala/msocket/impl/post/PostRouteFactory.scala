package msocket.impl.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.ErrorProtocol
import msocket.impl.RouteFactory
import msocket.impl.post.PostDirectives.withAcceptHeader
import msocket.impl.post.headers.AppNameHeader
import msocket.service.metrics.{Labelled, MetricCollector}

import scala.concurrent.ExecutionContext

class PostRouteFactory[Req: Decoder: ErrorProtocol: Labelled](endpoint: String, postHandler: HttpPostHandler[Req])(implicit
    ec: ExecutionContext
) extends RouteFactory[Req]
    with ServerHttpCodecs
    with HttpMetrics {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val counter = httpCounter

    post {
      path(endpoint) {
        optionalHeaderValueByName(AppNameHeader.name) { appName =>
          withAcceptHeader {
            withExceptionHandler {
              entity(as[Req]) { req =>
                extractClientIP { clientIp =>
                  val collector = new MetricCollector(metricsEnabled, req, appName, Some(counter), None, clientIp.toString())
                  withHttpMetrics(collector, postHandler.handle)
                }
              }
            }
          }
        }
      }
    }
  }
}
