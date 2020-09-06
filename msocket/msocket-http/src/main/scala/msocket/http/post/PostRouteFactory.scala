package msocket.http.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.ErrorProtocol
import msocket.http.RouteFactory
import msocket.http.post.PostDirectives.withAcceptHeader
import msocket.http.post.headers.AppNameHeader
import msocket.jvm.metrics.{LabelExtractor, MetricCollector}

import scala.concurrent.ExecutionContext

class PostRouteFactory[Req: Decoder: ErrorProtocol: LabelExtractor](endpoint: String, postHandler: HttpPostHandler[Req])(implicit
    ec: ExecutionContext
) extends RouteFactory[Req]
    with ServerHttpCodecs {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val counter = HttpMetrics.counter

    post {
      path(endpoint) {
        optionalHeaderValueByName(AppNameHeader.name) { appName =>
          withAcceptHeader {
            withExceptionHandler {
              entity(as[Req]) { req =>
                extractClientIP { clientIp =>
                  val collector = new MetricCollector(metricsEnabled, req, clientIp.toString(), appName, Some(counter), None)
                  collector.record()
                  postHandler.handle(req)
                }
              }
            }
          }
        }
      }
    }
  }
}
