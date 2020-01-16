package msocket.impl.rsocket.server

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.Decoder
import io.rsocket.{AbstractRSocket, Payload}
import msocket.api.{ContentType, ErrorProtocol}
import msocket.impl.rsocket.RSocketExtensions._
import reactor.core.publisher.{Flux, Mono}

import scala.compat.java8.FutureConverters.FutureOps

class RSocketImpl[Req: Decoder](
    requestResponseHandler: RSocketResponseHandler[Req],
    requestStreamHandler: RSocketStreamHandler[Req],
    contentType: ContentType
)(
    implicit actorSystem: ActorSystem[_],
    ep: ErrorProtocol[Req]
) extends AbstractRSocket {

  import actorSystem.executionContext
  val messageEncoder: RSocketPayloadEncoder[Req] = new RSocketPayloadEncoder[Req](contentType)

  override def requestResponse(payload: Payload): Mono[Payload] = {
    val payloadF = requestResponseHandler
      .handle(contentType.request(payload))
      .recover(messageEncoder.errorEncoder)

    Mono.fromCompletionStage(payloadF.toJava)
  }

  override def requestStream(payload: Payload): Flux[Payload] = {
    val value = Source
      .lazySingle[Req](() => contentType.request(payload))
      .flatMapConcat(requestStreamHandler.handle)
      .recover(messageEncoder.errorEncoder)

    Flux.from(value.runWith(Sink.asPublisher(false)))
  }
}
