package msocket.core.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink}
import akka.stream.{ActorMaterializer, Materializer}
import io.bullet.borer.Encoder

class ClientSocketSetup[RR: Encoder, RS: Encoder](webSocketRequest: WebSocketRequest)(implicit actorSystem: ActorSystem) {
  implicit val mat: Materializer = ActorMaterializer()
  import mat.executionContext

  private def pubSubPair() =
    MergeHub.source[Message](perProducerBufferSize = 16).toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both).run()

  val (upstreamSink, _upstreamSourceForFlow)     = pubSubPair()
  val (_downstreamSinkForFlow, downstreamSource) = pubSubPair()

  _upstreamSourceForFlow.runWith(Sink.ignore)
  downstreamSource.runWith(Sink.ignore)
  Thread.sleep(2000)

  private val flow: Flow[Message, Message, NotUsed] = Flow.fromSinkAndSource(_downstreamSinkForFlow, _upstreamSourceForFlow)

  private val (upgradeResponse, _) = Http().singleWebSocketRequest(WebSocketRequest("ws://localhost:5000/websocket"), flow)
  upgradeResponse.onComplete(println)
}
