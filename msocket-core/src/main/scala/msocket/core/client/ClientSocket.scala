package msocket.core.client

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.{Decoder, Encoder}
import msocket.core.api.{Encoding, MSocket, Envelope, Payload}

import scala.concurrent.Future

class ClientSocket[RR: Encoder: Decoder, RS: Decoder: Encoder](webSocketRequest: WebSocketRequest)(
    implicit actorSystem: ActorSystem,
    encoding: Encoding
) extends MSocket[RR, RS] {

  implicit val mat: Materializer = ActorMaterializer()
  import mat.executionContext

  private def makePair() =
    MergeHub.source[Message](perProducerBufferSize = 16).toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both).run()

  private val (upstreamSink, upstreamSource)     = makePair()
  private val (downstreamSink, downstreamSource) = makePair()

  private val flow: Flow[Message, Message, NotUsed] = Flow.fromSinkAndSource(downstreamSink, upstreamSource)

  private val (upgradeResponse, _) = Http().singleWebSocketRequest(WebSocketRequest("ws://localhost:5000/websocket"), flow)

  private val rrResponses: Source[Envelope[RR], NotUsed] = downstreamSource
    .collectType[TextMessage.Strict]
    .map(x => encoding.decode[Envelope[RR]](x.text))

  private val rsResponses: Source[Envelope[RS], NotUsed] = downstreamSource
    .collectType[TextMessage.Streamed]
    .flatMapMerge(1000, xs => xs.textStream.map(x => encoding.decode[Envelope[RS]](x)))

  def send(message: Payload[_], id: UUID): NotUsed = Source.single(encoding.strict(Envelope(message, id))).runWith(upstreamSink)

  override def requestResponse(message: RR): Future[Payload[_]]        = call(Payload(message), rrResponses).runWith(Sink.head)
  override def requestStream(message: RS): Source[Payload[_], NotUsed] = call(Payload(message), rsResponses)

  def call(payload: Payload[_], responses: Source[Envelope[_], NotUsed]): Source[Payload[_], NotUsed] = {
    val id = UUID.randomUUID()
    send(payload, id)
    responses.collect { case Envelope(response, `id`) => response }
  }

  upgradeResponse.onComplete(println)
}
