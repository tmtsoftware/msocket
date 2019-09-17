package mscoket.impl.rsocket.server

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.{Decoder, Json}
import io.rsocket.{AbstractRSocket, Payload}
import msocket.api.MessageHandler
import reactor.core.publisher.Flux

class RSocketImpl[Req: Decoder](requestHandler: MessageHandler[Req, Source[Payload, NotUsed]])(implicit mat: Materializer)
    extends AbstractRSocket {

  override def requestStream(payload: Payload): Flux[Payload] = {
    val value = requestHandler.handle(Json.decode(payload.getData).to[Req].value)
    Flux.from(value.runWith(Sink.asPublisher(false)))
  }
}
