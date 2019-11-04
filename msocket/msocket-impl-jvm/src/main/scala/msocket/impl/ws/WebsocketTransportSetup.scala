package msocket.impl.ws

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest}
import akka.stream.scaladsl.{Flow, Sink, Source}

class WebsocketTransportSetup(webSocketRequest: WebSocketRequest)(implicit actorSystem: ActorSystem[_]) {
  import actorSystem.executionContext

  def request(message: Message): Source[Message, NotUsed] = {
    val (connectionSink, connectionSource) =
      Source.asSubscriber[Message].mapMaterializedValue(Sink.fromSubscriber).preMaterialize()

    val requestSource        = Source.single(message).concat(Source.maybe)
    val flow                 = Flow.fromSinkAndSourceCoupled(connectionSink, requestSource)
    val (upgradeResponse, _) = Http()(actorSystem.toClassic).singleWebSocketRequest(webSocketRequest, flow)

    upgradeResponse.onComplete(println)
    connectionSource
  }
}
