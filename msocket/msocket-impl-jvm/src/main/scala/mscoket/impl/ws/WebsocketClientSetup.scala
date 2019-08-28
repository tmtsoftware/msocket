package mscoket.impl.ws

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}

class WebsocketClientSetup(webSocketRequest: WebSocketRequest)(implicit actorSystem: ActorSystem) {
  implicit val mat: Materializer = ActorMaterializer()
  import mat.executionContext

  def request(message: Message): Source[Message, NotUsed] = {
    val (connectionSink, connectionSource) =
      Source.asSubscriber[Message].mapMaterializedValue(Sink.fromSubscriber).preMaterialize()

    val requestSource        = Source.single(message).concat(Source.maybe)
    val flow                 = Flow.fromSinkAndSource(connectionSink, requestSource)
    val (upgradeResponse, _) = Http().singleWebSocketRequest(webSocketRequest, flow)

    upgradeResponse.onComplete(println)
    connectionSource
  }
}
