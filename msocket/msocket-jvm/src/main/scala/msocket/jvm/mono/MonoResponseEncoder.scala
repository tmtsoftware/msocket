package msocket.jvm.mono

import msocket.api.ErrorProtocol
import msocket.api.models.ResponseHeaders
import msocket.jvm.ResponseEncoder
import msocket.jvm.metrics.MetricCollector
import msocket.security.AccessController
import msocket.security.models.AccessStatus.Authorized

import scala.concurrent.{ExecutionContext, Future}

abstract class MonoResponseEncoder[Req: ErrorProtocol, M](implicit ec: ExecutionContext) extends ResponseEncoder[Req, M] {
  def accessController: AccessController

  def encodeMono(monoResponseF: Future[MonoResponse], collector: MetricCollector[Req]): Future[M] = {
    val future = monoResponseF.flatMap { monoResponse =>
      val eventualAuthStatus = accessController.authenticateAndAuthorize(monoResponse.authorizationPolicy)
      eventualAuthStatus
        .flatMap {
          case Authorized(accessToken) => monoResponse.responseFactory(accessToken)
          case x: RuntimeException     => Future.failed(x)
        }
        .map(res => encode(res, ResponseHeaders())(using monoResponse.encoder))
        .recover(errorEncoder)
    }

    future.map { result =>
      collector.record()
      result
    }

  }
}
