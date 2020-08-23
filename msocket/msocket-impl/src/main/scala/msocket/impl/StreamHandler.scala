package msocket.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import msocket.api.{ErrorProtocol, MessageEncoder, StreamResponse}
import msocket.impl.metrics.{MetricCollector, Metrics}

abstract class StreamHandler[Req: ErrorProtocol, M] extends MessageEncoder[Req, M] {
  def handle(streamResponse: StreamResponse, collector: MetricCollector[Req]): Source[M, NotUsed] = {
    val stream = streamResponse.responseStream
      .map(res => encode(res)(streamResponse.encoder))
      .recover(errorEncoder)
      .mapMaterializedValue(_ => NotUsed)
    withMetrics(stream, collector)
  }

  def withMetrics[Msg](stream: Source[Msg, NotUsed], collector: MetricCollector[Req]): Source[Msg, NotUsed] =
    Metrics.withMetrics(stream, collector)
}
