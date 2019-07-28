package msocket.core.client

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.{Encoding, Payload}

import scala.concurrent.Future

class ClientSocketImpl[RR: Encoder, RS: Encoder](webSocketRequest: WebSocketRequest)(
    implicit actorSystem: ActorSystem,
    encoding: Encoding
) extends ClientSocket[RR, RS] {

  private val setup              = new ClientSocketSetup[RR, RS](webSocketRequest)
  implicit val mat: Materializer = ActorMaterializer()

  override def requestResponse[Res: Decoder: Encoder](message: RR): Future[Res] = {
    val Id = UUID.randomUUID()
    send(Payload(message), Id)
    rrResponses(Id).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder: Encoder](message: RS): Source[Res, NotUsed] = {
    val Id = UUID.randomUUID()
    send(Payload(message), Id)
    rsResponses(Id)
  }

  private def send(message: Payload[_], id: UUID): NotUsed = {
    Source.single(encoding.strict(message)).runWith(setup.upstreamSink)
  }

  private def rrResponses[Res: Decoder: Encoder](UUID: UUID): Source[Res, NotUsed] =
    setup.downstreamSource
      .collectType[TextMessage.Strict]
      .map { x =>
        println(x)
        encoding.decode[Payload[Res]](x.text)
      }
      .map(_.value)

  private def rsResponses[Res: Decoder: Encoder](UUID: UUID): Source[Res, NotUsed] =
    setup.downstreamSource
      .collectType[TextMessage.Streamed]
      .flatMapMerge(
        1000,
        xs => xs.textStream.map(x => encoding.decode[Payload[Res]](x)).map(_.value)
      )
}
