package msocket.impl.rsocket.server

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.Decoder
import io.rsocket.{AbstractRSocket, Payload}
import msocket.api.{ContentType, ErrorProtocol}
import msocket.impl.rsocket.RSocketExtensions._
import reactor.core.publisher.{Flux, Mono}

import scala.compat.java8.FutureConverters.FutureOps

class RSocketImpl[RespReq: Decoder: ErrorProtocol, StreamReq: Decoder](
    requestResponseHandlerF: ContentType => RSocketResponseHandler[RespReq],
    requestStreamHandlerF: ContentType => RSocketStreamHandler[StreamReq],
    contentType: ContentType
)(implicit actorSystem: ActorSystem[_])
    extends AbstractRSocket {

  import actorSystem.executionContext
  val messageEncoder: RSocketPayloadEncoder[RespReq] = new RSocketPayloadEncoder[RespReq](contentType)

  private lazy val requestResponseHandler = requestResponseHandlerF(contentType)
  private lazy val requestStreamHandler   = requestStreamHandlerF(contentType)

  override def requestResponse(payload: Payload): Mono[Payload] = {
    val payloadF = requestResponseHandler
      .handle(contentType.request[RespReq](payload))
      .recover(messageEncoder.errorEncoder)

    Mono.fromCompletionStage(payloadF.toJava)
  }

  override def requestStream(payload: Payload): Flux[Payload] = {
    val value = Source
      .lazySingle(() => contentType.request[StreamReq](payload))
      .flatMapConcat(requestStreamHandler.handle)
      .recover(messageEncoder.errorEncoder)

    Flux.from(value.runWith(Sink.asPublisher(false)))
  }
}
