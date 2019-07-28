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

class ClientHandler(webSocketRequest: WebSocketRequest)(implicit actorSystem: ActorSystem, encoding: Encoding) {
  implicit val mat: Materializer = ActorMaterializer()

  private def makePair() =
    MergeHub.source[Message](perProducerBufferSize = 16).toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both).run()

  val (upstreamSink, upstreamSource)     = makePair()
  val (downstreamSink, downstreamSource) = makePair()

  val flow: Flow[Message, Message, NotUsed] = Flow.fromSinkAndSource(downstreamSink, upstreamSource)

  val (upgradeResponse, closed) = Http().singleWebSocketRequest(WebSocketRequest("ws://localhost:5000/websocket"), flow)

  def textSocket[RR <: Any: Encoder: Decoder, RS: Decoder: Encoder](): MSocket[RR, RS] = new MSocket[RR, RS] {

    val rrResponses: Source[Envelope[RR], NotUsed] = downstreamSource
      .collectType[TextMessage.Strict]
      .map(x => encoding.decode[Envelope[RR]](x.text))

    val rsResponses: Source[Envelope[RS], NotUsed] = downstreamSource
      .collectType[TextMessage.Streamed]
      .flatMapMerge(1000, xs => xs.textStream.map(x => encoding.decode[Envelope[RS]](x)))

    def send(message: Payload[_], id: UUID): NotUsed = Source.single(encoding.strict(Envelope(message, id))).runWith(upstreamSink)

    override def requestResponse(message: RR): Future[Payload[_]] = {
      val id = UUID.randomUUID()
      send(Payload(message), id)
      rrResponses.collect { case Envelope(response, `id`) => response }.runWith(Sink.head)
    }

    override def requestStream(message: RS): Source[Payload[_], NotUsed] = {
      val id = UUID.randomUUID()
      send(Payload(message), id)
      rsResponses.collect { case Envelope(response, `id`) => response }
    }
  }
}
