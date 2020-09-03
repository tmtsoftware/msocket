package msocket.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import msocket.api.models.ResponseHeaders
import msocket.api.{ErrorProtocol, ResponseEncoder, StreamResponse}
import msocket.impl.metrics.{MetricCollector, Metrics}

import scala.concurrent.Future

abstract class ResponseStreamHandler[Req: ErrorProtocol, M] extends ResponseEncoder[Req, M] {
  def handle(streamResponseF: Future[StreamResponse], collector: MetricCollector[Req]): Source[M, NotUsed] = {
    val stream = Source.future(streamResponseF).flatMapConcat { streamResponse =>
      streamResponse.responseStream
        .map(res => encode(res, ResponseHeaders())(streamResponse.encoder))
        .recover(errorEncoder)
        .mapMaterializedValue(_ => NotUsed)
    }

    withMetrics(stream, collector)
  }

  def withMetrics[Msg](stream: Source[Msg, NotUsed], collector: MetricCollector[Req]): Source[Msg, NotUsed] =
    Metrics.withMetrics(stream, collector)
}
