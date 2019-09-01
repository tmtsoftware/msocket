package mscoket.impl.ws

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest}
import akka.stream.scaladsl.{BroadcastHub, Flow, MergeHub, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}

class WebsocketClientSetup(webSocketRequest: WebSocketRequest)(implicit actorSystem: ActorSystem) {
  implicit val mat: Materializer = ActorMaterializer()
  import mat.executionContext

  val (connectionSink, connectionSource) =
    Source.asSubscriber[Message].mapMaterializedValue(Sink.fromSubscriber).preMaterialize()

  val broadcastHub: Source[Message, NotUsed] = connectionSource.runWith(BroadcastHub.sink(2048))

  val (sink, source) =
    Source.asSubscriber[Message].mapMaterializedValue(Sink.fromSubscriber).preMaterialize()

  val mergeHub: Sink[Message, NotUsed] = MergeHub.source[Message].to(sink).run()

  val flow: Flow[Message, Message, NotUsed] = Flow.fromSinkAndSource(connectionSink, source)
  val (upgradeResponse, _)                  = Http().singleWebSocketRequest(webSocketRequest, flow)
  upgradeResponse.onComplete(println)

  broadcastHub.runWith(Sink.ignore)

  def request(message: Message): Source[Message, NotUsed] = {
    Source.single(message).runWith(mergeHub)
    broadcastHub
  }

  //Source.single(message).concat(Source.maybe).runWith(mergeHub)

}
