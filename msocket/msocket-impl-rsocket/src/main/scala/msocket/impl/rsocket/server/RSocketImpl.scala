package msocket.impl.rsocket.server

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.Decoder
import io.rsocket.util.DefaultPayload
import io.rsocket.{AbstractRSocket, Payload}
import msocket.api.Encoding.CborByteBuffer
import msocket.api.MessageHandler
import msocket.api.models.ServiceError
import reactor.core.publisher.Flux

import scala.util.control.NonFatal

class RSocketImpl[Req: Decoder](requestHandler: MessageHandler[Req, Source[Payload, NotUsed]])(implicit actorSystem: ActorSystem[_])
    extends AbstractRSocket {

  override def requestStream(payload: Payload): Flux[Payload] = {
    val value = Source
      .lazySingle[Req](() => CborByteBuffer.decodeWithServiceError(payload.getData))
      .flatMapConcat(requestHandler.handle)
      .recover {
        case NonFatal(ex) => DefaultPayload.create(CborByteBuffer.encode(ServiceError.fromThrowable(ex)))
      }

    Flux.from(value.runWith(Sink.asPublisher(false)))
  }
}
