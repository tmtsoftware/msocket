package msocket.impl.rsocket.server

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.Decoder
import io.rsocket.{AbstractRSocket, Payload}
import msocket.api.Encoding.CborByteBuffer
import msocket.api.ErrorProtocol
import reactor.core.publisher.{Flux, Mono}

import scala.compat.java8.FutureConverters.FutureOps

class RSocketImpl[Req: Decoder](requestResponseHandler: RSocketResponseHandler[Req], requestStreamHandler: RSocketStreamHandler[Req])(
    implicit actorSystem: ActorSystem[_],
    ep: ErrorProtocol[Req]
) extends AbstractRSocket {

  import actorSystem.executionContext
  val messageEncoder: RSocketPayloadEncoder[Req] = new RSocketPayloadEncoder[Req]

  override def requestResponse(payload: Payload): Mono[Payload] = {
    val payloadF = requestResponseHandler
      .handle(CborByteBuffer.decode(payload.getData))
      .recover(messageEncoder.errorEncoder)

    Mono.fromCompletionStage(payloadF.toJava)
  }

  override def requestStream(payload: Payload): Flux[Payload] = {
    val value = Source
      .lazySingle[Req](() => CborByteBuffer.decode(payload.getData))
      .flatMapConcat(requestStreamHandler.handle)
      .recover(messageEncoder.errorEncoder)

    Flux.from(value.runWith(Sink.asPublisher(false)))
  }
}
