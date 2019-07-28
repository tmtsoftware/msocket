package msocket.core.client

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.{Encoding, Envelope, MClientSocket, Payload}

import scala.concurrent.Future

class ClientSocket[RR: Encoder, RS: Encoder](webSocketRequest: WebSocketRequest)(
    implicit actorSystem: ActorSystem,
    encoding: Encoding
) extends MClientSocket[RR, RS] {

  implicit val mat: Materializer = ActorMaterializer()
  import mat.executionContext

  private def pubSubPair() =
    MergeHub.source[Message](perProducerBufferSize = 16).toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both).run()

  private val (upstreamSink, _upstreamSourceForFlow)     = pubSubPair()
  private val (_downstreamSinkForFlow, downstreamSource) = pubSubPair()

  private val flow: Flow[Message, Message, NotUsed] = Flow.fromSinkAndSource(_downstreamSinkForFlow, _upstreamSourceForFlow)

  private val (upgradeResponse, _) = Http().singleWebSocketRequest(WebSocketRequest("ws://localhost:5000/websocket"), flow)
  upgradeResponse.onComplete(println)

  private def rrResponses[Res: Decoder: Encoder]: Source[Envelope[Res], NotUsed] =
    downstreamSource
      .collectType[TextMessage.Strict]
      .map(x => encoding.decode[Envelope[Res]](x.text))

  private def rsResponses[Res: Decoder: Encoder]: Source[Envelope[Res], NotUsed] =
    downstreamSource
      .collectType[TextMessage.Streamed]
      .flatMapMerge(1000, xs => xs.textStream.map(x => encoding.decode[Envelope[Res]](x)))

  override def requestResponse[Res: Decoder: Encoder](message: RR): Future[Payload[Res]] = {
    call(Payload(message), rrResponses).runWith(Sink.head)
  }

  override def requestStream[Res: Decoder: Encoder](message: RS): Source[Payload[Res], NotUsed] = {
    call(Payload(message), rsResponses)
  }

  private def call[Res: Decoder: Encoder](
      payload: Payload[_],
      responses: Source[Envelope[Res], NotUsed]
  ): Source[Payload[Res], NotUsed] = {
    val id = UUID.randomUUID()
    send(payload, id)
    responses.collect { case Envelope(response, `id`) => response }
  }

  private def send(message: Payload[_], id: UUID): NotUsed = {
    Source.single(encoding.strict(Envelope(message, id))).runWith(upstreamSink)
  }
}
