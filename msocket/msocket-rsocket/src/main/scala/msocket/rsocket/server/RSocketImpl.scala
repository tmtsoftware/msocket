package msocket.rsocket.server

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import io.bullet.borer.Decoder
import io.prometheus.client.Counter
import io.rsocket.{Payload, RSocket}
import msocket.api.{ContentType, ErrorProtocol}
import msocket.jvm.metrics.{LabelExtractor, MetricCollector}
import msocket.jvm.mono.MonoRequestHandler
import msocket.jvm.stream.StreamRequestHandler
import msocket.rsocket.RSocketExtensions._
import msocket.security.AccessControllerFactory
import reactor.core.publisher.{Flux, Mono}

import scala.compat.java8.FutureConverters.FutureOps
import scala.concurrent.Future

class RSocketImpl[Req: Decoder: ErrorProtocol: LabelExtractor, StreamReq: Decoder: ErrorProtocol: LabelExtractor](
    monoRequestHandler: MonoRequestHandler[Req],
    streamRequestHandler: StreamRequestHandler[StreamReq],
    contentType: ContentType,
    accessControllerFactory: AccessControllerFactory,
    metricsEnabled: Boolean = false
)(implicit actorSystem: ActorSystem[_])
    extends RSocket {

  import actorSystem.executionContext

  private lazy val monoResponseEncoder   = new RSocketMonoResponseEncoder[Req](contentType, accessControllerFactory.make(None))
  private lazy val streamResponseEncoder = new RSocketStreamResponseEncoder[StreamReq](contentType, accessControllerFactory.make(None))

  private lazy val streamGauge   = RSocketStreamRequestMetrics.gauge[StreamReq]()
  private lazy val perMsgCounter = RSocketStreamRequestMetrics.counter[StreamReq]()

  private lazy val monoCounter: Counter = RSocketMonoRequestMetrics.counter[Req]

  override def requestResponse(payload: Payload): Mono[Payload] = {
    val payloadF = Future(contentType.request[Req](payload))
      .flatMap { req =>
        val collector = new MetricCollector(metricsEnabled, req, Some("TODO"), Some("TODO"), Some(monoCounter), None)
        monoResponseEncoder.encodeMono(monoRequestHandler.handle(req), collector)
      }
      .recover(monoResponseEncoder.errorEncoder)
    Mono.fromCompletionStage(payloadF.toJava)
  }

  override def requestStream(payload: Payload): Flux[Payload] = {
    val value = Source
      .lazySingle(() => contentType.request[StreamReq](payload))
      .flatMapConcat { req =>
        val collector =
          new MetricCollector[StreamReq](metricsEnabled, req, Some("TODO"), Some("TODO"), Some(perMsgCounter), Some(streamGauge))
        streamResponseEncoder.encodeStream(streamRequestHandler.handle(req), collector)
      }
      .recover(streamResponseEncoder.errorEncoder)

    Flux.from(value.runWith(Sink.asPublisher(false)))
  }
}
