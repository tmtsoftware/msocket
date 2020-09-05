package msocket.impl.rsocket.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Dom.Element
import io.bullet.borer.Encoder
import io.rsocket.Payload
import msocket.api.models.{ResponseHeaders, ServiceError}
import msocket.api.{ContentType, ErrorProtocol}
import msocket.impl.rsocket.RSocketExtensions._
import msocket.security.AccessController
import msocket.service.{StreamResponse, StreamResponseEncoder}
import msocket.service.metrics.MetricCollector

import scala.concurrent.Future

/**
 * This helper class can be extended to define custom RSocket handler in the server which returns [[Source]] of [[Payload]].
 * RSocketStreamHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
class RSocketStreamResponseEncoder[Req: ErrorProtocol](contentType: ContentType, val accessController: AccessController)
    extends StreamResponseEncoder[Req, Payload] {
  override def encode[Res: Encoder](response: Res, headers: ResponseHeaders): Payload                                = contentType.payload(response, headers)
  override def withMetrics[Msg](stream: Source[Msg, NotUsed], collector: MetricCollector[Req]): Source[Msg, NotUsed] = stream
}

object RSocketStreamResponseEncoder {
  val Missing: (ContentType, AccessController) => RSocketStreamResponseEncoder[Element] = { (contentType, accessController) =>
    new RSocketStreamResponseEncoder[Element](contentType, accessController)(ErrorProtocol.bind[Element, ServiceError]) {
      override def handle(streamResponseF: Future[StreamResponse], collector: MetricCollector[Element]): Source[Payload, NotUsed] = {
        Source.failed(new RuntimeException("missing stream handler"))
      }
    }
  }
}
