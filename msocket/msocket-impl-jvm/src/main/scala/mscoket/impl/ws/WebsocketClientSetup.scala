package mscoket.impl.ws

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import mscoket.impl.ws.Encoding.JsonText

class WebsocketClientSetup(webSocketRequest: WebSocketRequest)(implicit actorSystem: ActorSystem) {
  implicit val mat: Materializer = ActorMaterializer()
  import mat.executionContext

  val (connectionSink, connectionSource) =
    Source.asSubscriber[Message].mapMaterializedValue(Sink.fromSubscriber).preMaterialize()

  val broadcastHub: Source[(String, Source[String, NotUsed]), NotUsed] = connectionSource
    .collect {
      case TextMessage.Streamed(items) =>
        println("*********")
        items
    }
    .mapAsync(10000)(items => items.prefixAndTail(1).runWith(Sink.head))
    .collect {
      case (Seq(x), xs) =>
        JsonText.decodeText[String](x) -> xs.map(x => JsonText.decodeText[String](x))
    }
    .watchTermination()(Keep.right)
    .mapMaterializedValue { terminationF =>
      terminationF.onComplete(x => println(s"-------> $x"))
      NotUsed
    }
    .runWith(BroadcastHub.sink(2048))

  val (sink, source) =
    Source.asSubscriber[Message].mapMaterializedValue(Sink.fromSubscriber).preMaterialize()

  val mergeHub: Sink[Message, NotUsed] = MergeHub.source[Message].to(sink).run()

  val flow: Flow[Message, Message, NotUsed] = Flow.fromSinkAndSource(connectionSink, source)
  val (upgradeResponse, _)                  = Http().singleWebSocketRequest(webSocketRequest, flow)
  upgradeResponse.onComplete(println)

  broadcastHub.runWith(Sink.ignore)

  def request(message: Message): Source[(String, Source[String, NotUsed]), NotUsed] = {
    Source.single(message).runWith(mergeHub)
    broadcastHub
  }

  //Source.single(message).concat(Source.maybe).runWith(mergeHub)

}
