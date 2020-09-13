package msocket.jvm.mono

import msocket.api.ErrorProtocol
import msocket.api.models.ResponseHeaders
import msocket.jvm.ResponseEncoder
import msocket.jvm.metrics.MetricCollector
import msocket.security.AccessController
import msocket.security.models.AccessStatus

import scala.concurrent.{ExecutionContext, Future}

abstract class MonoResponseEncoder[Req: ErrorProtocol, M](implicit ec: ExecutionContext) extends ResponseEncoder[Req, M] {
  def accessController: AccessController

  def encodeMono(monoResponseF: Future[MonoResponse], collector: MetricCollector[Req]): Future[M] = {
    val future = monoResponseF.flatMap { monoResponse =>
      accessController.check(monoResponse.authorizationPolicy).flatMap {
        case AccessStatus.Authorized(accessToken)                =>
          monoResponse
            .responseFactory(accessToken)
            .map(res => encode(res, ResponseHeaders())(monoResponse.encoder))
            .recover(errorEncoder)
        case failedAccessStatus: AccessStatus.FailedAccessStatus =>
          Future.failed(failedAccessStatus)
      }
    }

    future.map { result =>
      collector.record()
      result
    }

  }
}
