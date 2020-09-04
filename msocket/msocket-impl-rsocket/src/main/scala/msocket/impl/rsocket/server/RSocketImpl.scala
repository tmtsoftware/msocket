package msocket.impl.rsocket.server

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.Decoder
import io.rsocket.{Payload, RSocket}
import msocket.api.security.AccessControllerFactory
import msocket.api.{ContentType, ErrorProtocol, StreamRequestHandler}
import msocket.impl.rsocket.RSocketExtensions._
import reactor.core.publisher.{Flux, Mono}

import scala.compat.java8.FutureConverters.FutureOps

class RSocketImpl[RespReq: Decoder, StreamReq: Decoder: ErrorProtocol](
    requestResponseHandlerF: ContentType => RSocketResponseHandler[RespReq],
    streamRequestHandler: StreamRequestHandler[StreamReq],
    contentType: ContentType,
    accessControllerFactory: AccessControllerFactory
)(implicit actorSystem: ActorSystem[_])
    extends RSocket {

  import actorSystem.executionContext

  private lazy val requestResponseHandler = requestResponseHandlerF(contentType)
  private lazy val rSocketStreamHandler   = new RSocketStreamResponseEncoder[StreamReq](contentType, accessControllerFactory.make(None))

  override def requestResponse(payload: Payload): Mono[Payload] = {
    val payloadF = requestResponseHandler
      .handle(contentType.request[RespReq](payload))
      .recover(requestResponseHandler.errorEncoder)

    Mono.fromCompletionStage(payloadF.toJava)
  }

  override def requestStream(payload: Payload): Flux[Payload] = {
    val value = Source
      .lazySingle(() => contentType.request[StreamReq](payload))
      .flatMapConcat(req => rSocketStreamHandler.handle(streamRequestHandler.handle(req), null))
      .recover(rSocketStreamHandler.errorEncoder)

    Flux.from(value.runWith(Sink.asPublisher(false)))
  }
}
