package msocket.impl.rsocket.server

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.Decoder
import io.rsocket.util.DefaultPayload
import io.rsocket.{AbstractRSocket, Payload}
import msocket.api.Encoding.CborByteBuffer
import msocket.api.models.ServiceError
import msocket.api.{ErrorProtocol, MessageHandler}
import reactor.core.publisher.{Flux, Mono}

import scala.compat.java8.FutureConverters.FutureOps
import scala.concurrent.Future
import scala.util.control.NonFatal

class RSocketImpl[Req: Decoder](
    requestResponseHandler: MessageHandler[Req, Future[Payload]],
    requestStreamHandler: MessageHandler[Req, Source[Payload, NotUsed]]
)(
    implicit actorSystem: ActorSystem[_],
    ep: ErrorProtocol[Req]
) extends AbstractRSocket {

  import actorSystem.executionContext

  override def requestResponse(payload: Payload): Mono[Payload] = {
    val payloadF = requestResponseHandler
      .handle(CborByteBuffer.decode(payload.getData))
      .recover(onError())

    Mono.fromCompletionStage(payloadF.toJava)
  }

  override def requestStream(payload: Payload): Flux[Payload] = {
    val value = Source
      .lazySingle[Req](() => CborByteBuffer.decode(payload.getData))
      .flatMapConcat(requestStreamHandler.handle)
      .recover(onError())

    Flux.from(value.runWith(Sink.asPublisher(false)))
  }

  def onError(): PartialFunction[Throwable, Payload] = {
    case NonFatal(ex: ep.E) => DefaultPayload.create(CborByteBuffer.encode(ex))
    case NonFatal(ex)       => DefaultPayload.create(CborByteBuffer.encode(ServiceError.fromThrowable(ex)))
  }

}
