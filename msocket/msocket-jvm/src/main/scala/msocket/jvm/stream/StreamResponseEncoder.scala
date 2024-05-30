package msocket.jvm.stream

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Source
import msocket.api.ErrorProtocol
import msocket.api.models.ResponseHeaders
import msocket.jvm.ResponseEncoder
import msocket.jvm.metrics.MetricCollector
import msocket.security.AccessController
import msocket.security.models.AccessStatus.Authorized

import scala.concurrent.Future

abstract class StreamResponseEncoder[Req: ErrorProtocol, M] extends ResponseEncoder[Req, M] {
  def accessController: AccessController

  def encodeStream(streamResponseF: Future[StreamResponse], collector: MetricCollector[Req]): Source[M, NotUsed] = {
    val stream = Source
      .future(streamResponseF)
      .flatMapConcat { streamResponse =>
        val eventualAuthStatus = accessController.authenticateAndAuthorize(streamResponse.authorizationPolicy)
        Source
          .future(eventualAuthStatus)
          .flatMapConcat {
            case Authorized(accessToken)      => streamResponse.responseFactory(accessToken)
            case authStatus: RuntimeException => Source.failed(authStatus)
          }
          .map(res => encode(res, ResponseHeaders())(using streamResponse.encoder))
          .recover(errorEncoder)
          .mapMaterializedValue(_ => NotUsed)
      }

    collector.streamMetric(stream)
  }
}
