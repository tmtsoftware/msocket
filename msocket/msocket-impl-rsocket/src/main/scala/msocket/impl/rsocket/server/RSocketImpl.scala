package msocket.impl.rsocket.server

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import io.bullet.borer.Decoder
import io.rsocket.util.DefaultPayload
import io.rsocket.{AbstractRSocket, Payload}
import msocket.api.MessageHandler
import msocket.api.models.ServiceException
import msocket.impl.Encoding.CborBinary
import reactor.core.publisher.Flux

import scala.util.control.NonFatal

class RSocketImpl[Req: Decoder](requestHandler: MessageHandler[Req, Source[Payload, NotUsed]])(implicit actorSystem: ActorSystem[_])
    extends AbstractRSocket {

  override def requestStream(payload: Payload): Flux[Payload] = {
    val value = Source
      .lazySingle[Req](() => CborBinary.decodeWithServiceException(ByteString.fromByteBuffer(payload.getData)))
      .flatMapConcat(requestHandler.handle)
      .recover {
        case NonFatal(ex) => DefaultPayload.create(CborBinary.encode(ServiceException.fromThrowable(ex)).asByteBuffer)
      }

    Flux.from(value.runWith(Sink.asPublisher(false)))
  }
}
