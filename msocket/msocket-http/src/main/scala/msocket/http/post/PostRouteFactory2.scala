package msocket.http.post

import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.{Directive0, Route}
import io.bullet.borer.Decoder
import msocket.api.ErrorProtocol
import msocket.http.RouteFactory
import msocket.http.post.PostDirectives.withAcceptHeader
import msocket.http.post.headers.{AppNameHeader, UserNameHeader}
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
              optionalHeaderValueByName(UserNameHeader.name) { username =>
                entity(as[Req]) { req =>
                  val collector = new MetricCollector(metricsEnabled, req, appName, username, Some(counter), None)
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
