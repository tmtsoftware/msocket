package msocket.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import msocket.api.models.ResponseHeaders
import msocket.security.api.AccessStatus
import msocket.api.{ErrorProtocol, ResponseEncoder, StreamResponse}
import msocket.impl.metrics.{MetricCollector, Metrics}
import msocket.security.api.AccessController

import scala.concurrent.Future

abstract class StreamResponseEncoder[Req: ErrorProtocol, M] extends ResponseEncoder[Req, M] {
  def accessController: AccessController

  def handle(streamResponseF: Future[StreamResponse], collector: MetricCollector[Req]): Source[M, NotUsed] = {
    val stream = Source.future(streamResponseF).flatMapConcat { streamResponse =>
      Source.future(accessController.check(streamResponse.authorizationPolicy)).flatMapConcat {
        case AccessStatus.Authorized                             =>
          streamResponse.responseStream
            .map(res => encode(res, ResponseHeaders())(streamResponse.encoder))
            .recover(errorEncoder)
            .mapMaterializedValue(_ => NotUsed)
        case failedAccessStatus: AccessStatus.FailedAccessStatus =>
          Source.failed(failedAccessStatus)
      }
    }

    withMetrics(stream, collector)
  }

  def withMetrics[Msg](stream: Source[Msg, NotUsed], collector: MetricCollector[Req]): Source[Msg, NotUsed] =
    Metrics.withMetrics(stream, collector)
}
