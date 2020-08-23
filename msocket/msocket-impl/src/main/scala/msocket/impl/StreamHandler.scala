package msocket.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import msocket.api.{ErrorProtocol, MessageEncoder, StreamResponse}
import msocket.impl.metrics.{MetricCollector, Metrics}

import scala.concurrent.Future

abstract class StreamHandler[Req: ErrorProtocol, M] extends MessageEncoder[Req, M] {
  def handle(streamResponseF: Future[StreamResponse], collector: MetricCollector[Req]): Source[M, NotUsed] = {
    val stream = Source.future(streamResponseF).flatMapConcat { streamResponse =>
      streamResponse.responseStream
        .map(res => encode(res)(streamResponse.encoder))
        .recover(errorEncoder)
        .mapMaterializedValue(_ => NotUsed)
    }

    withMetrics(stream, collector)
  }

  def withMetrics[Msg](stream: Source[Msg, NotUsed], collector: MetricCollector[Req]): Source[Msg, NotUsed] =
    Metrics.withMetrics(stream, collector)
}
