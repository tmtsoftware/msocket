package msocket.core.client

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.{Encoding, Envelope, Payload}

import scala.concurrent.Future

class ClientSocketImpl[RR: Encoder, RS: Encoder](webSocketRequest: WebSocketRequest)(
    implicit actorSystem: ActorSystem,
    encoding: Encoding
) extends ClientSocket[RR, RS] {

  private val setup              = new ClientSocketSetup[RR, RS](webSocketRequest)
  implicit val mat: Materializer = ActorMaterializer()

  override def requestResponse[Res: Decoder: Encoder](message: RR): Future[Res] = {
    call(Payload(message), rrResponses).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder: Encoder](message: RS): Source[Res, NotUsed] = {
    call(Payload(message), rsResponses)
  }

  private def call[Res](
      payload: Payload[_],
      responses: Source[Envelope[Res], NotUsed]
  ): Source[Res, NotUsed] = {
    val id = UUID.randomUUID()
    send(payload, id)
    responses.collect { case Envelope(response, `id`) => response.value }
  }

  private def send(message: Payload[_], id: UUID): NotUsed = {
    Source.single(encoding.strict(Envelope(message, id))).runWith(setup.upstreamSink)
  }

  private def rrResponses[Res: Decoder: Encoder]: Source[Envelope[Res], NotUsed] =
    setup.downstreamSource
      .collectType[TextMessage.Strict]
      .map { x =>
        println(x)
        encoding.decode[Envelope[Res]](x.text)
      }

  private def rsResponses[Res: Decoder: Encoder]: Source[Envelope[Res], NotUsed] =
    setup.downstreamSource
      .collectType[TextMessage.Streamed]
      .flatMapMerge(1000, xs => xs.textStream.map(x => encoding.decode[Envelope[Res]](x)))
}
