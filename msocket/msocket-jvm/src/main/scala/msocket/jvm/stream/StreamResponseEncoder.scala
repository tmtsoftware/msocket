package msocket.jvm.stream

import akka.NotUsed
import akka.stream.scaladsl.Source
import msocket.api.ErrorProtocol
import msocket.api.models.ResponseHeaders
import msocket.jvm.ResponseEncoder
import msocket.jvm.metrics.MetricCollector
import msocket.security.AccessController
import msocket.security.models.AccessStatus

import scala.concurrent.Future

abstract class StreamResponseEncoder[Req: ErrorProtocol, M] extends ResponseEncoder[Req, M] {
  def accessController: AccessController

  def encodeStream(streamResponseF: Future[StreamResponse], collector: MetricCollector[Req]): Source[M, NotUsed] = {
    val stream = Source.future(streamResponseF).flatMapConcat { streamResponse =>
      Source.future(accessController.check(streamResponse.authorizationPolicy)).flatMapConcat {
        case AccessStatus.Authorized(accessToken)                =>
          streamResponse
            .responseFactory(accessToken)
            .map(res => encode(res, ResponseHeaders())(streamResponse.encoder))
            .recover(errorEncoder)
            .mapMaterializedValue(_ => NotUsed)
        case failedAccessStatus: AccessStatus.FailedAccessStatus =>
          Source.failed(failedAccessStatus)
      }
    }

    collector.streamMetric(stream)
  }
}
