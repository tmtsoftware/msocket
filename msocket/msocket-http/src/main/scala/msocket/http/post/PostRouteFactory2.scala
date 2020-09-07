package msocket.http.post

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.ErrorProtocol
import msocket.http.RouteFactory
import msocket.http.post.PostDirectives.withAcceptHeader
import msocket.http.post.headers.AppNameHeader
import msocket.jvm.metrics.{LabelExtractor, MetricCollector}
import msocket.jvm.mono.MonoRequestHandler
import msocket.security.AccessControllerFactory

import scala.concurrent.ExecutionContext

class PostRouteFactory2[Req: Decoder: ErrorProtocol: LabelExtractor](
    endpoint: String,
    requestHandler: MonoRequestHandler[Req],
    accessControllerFactory: AccessControllerFactory
)(implicit
    ec: ExecutionContext
) extends RouteFactory[Req]
    with ServerHttpCodecs {

  private val withExceptionHandler: Directive0 = PostDirectives.exceptionHandlerFor[Req]

  def make(metricsEnabled: Boolean = false): Route = {
    lazy val counter    = HttpMetrics.counter2
    val responseEncoder = new HttpResponseEncoder[Req](accessControllerFactory.make(None))

    post {
      path(endpoint) {
        withAcceptHeader {
          withExceptionHandler {
            optionalHeaderValueByName(AppNameHeader.name) { appName =>
              extractClientIP { clientIp =>
                entity(as[Req]) { req =>
                  val collector = new MetricCollector(metricsEnabled, req, clientIp.toString(), appName, Some(counter), None)
                  val routeF    = responseEncoder.encodeMono(requestHandler.handle(req), collector)
                  onSuccess(routeF)(identity)
                }
              }
            }
          }
        }
      }
    }
  }
}