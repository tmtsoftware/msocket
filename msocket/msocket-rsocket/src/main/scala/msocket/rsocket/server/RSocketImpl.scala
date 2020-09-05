package msocket.rsocket.server

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.Decoder
import io.prometheus.client.Counter
import io.rsocket.{Payload, RSocket}
import msocket.api.{ContentType, ErrorProtocol}
import msocket.jvm.metrics.MetricCollector
import msocket.jvm.mono.MonoRequestHandler
import msocket.jvm.stream.StreamRequestHandler
import msocket.rsocket.RSocketExtensions._
import msocket.security.AccessControllerFactory
import reactor.core.publisher.{Flux, Mono}

import scala.compat.java8.FutureConverters.FutureOps

class RSocketImpl[Req: Decoder: ErrorProtocol, StreamReq: Decoder: ErrorProtocol](
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

  private lazy val streamRequestMetrics = new RSocketStreamRequestMetrics {}
  private lazy val streamGauge          = streamRequestMetrics.rSocketStreamGauge
  private lazy val perMsgCounter        = streamRequestMetrics.rSocketStreamPerMsgCounter

  private lazy val monoRequestMetrics   = new RSocketMonoRequestMetrics {}
  private lazy val monoCounter: Counter = monoRequestMetrics.rSocketMonoCounter

  override def requestResponse(payload: Payload): Mono[Payload] = {
    val req       = contentType.request[Req](payload)
    val collector = new MetricCollector(metricsEnabled, req, Some("TODO"), Some(monoCounter), None, "TODO")
    val payloadF  = monoResponseEncoder
      .encodeMono(monoRequestHandler.handle(req), collector)
      .recover(monoResponseEncoder.errorEncoder)

    Mono.fromCompletionStage(payloadF.toJava)
  }

  override def requestStream(payload: Payload): Flux[Payload] = {
    val value = Source
      .lazySingle(() => contentType.request[StreamReq](payload))
      .flatMapConcat { req =>
        val collector = new MetricCollector[StreamReq](metricsEnabled, req, Some("TODO"), Some(perMsgCounter), Some(streamGauge), "TODO")
        streamResponseEncoder.encodeStream(streamRequestHandler.handle(req), collector)
      }
      .recover(streamResponseEncoder.errorEncoder)

    Flux.from(value.runWith(Sink.asPublisher(false)))
  }
}
