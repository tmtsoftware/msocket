package msocket.simple.client

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import csw.simple.api.Codecs
import csw.simple.api.Protocol.Hello
import msocket.core.api.{MResponse, Payload}
import msocket.core.extensions.ToMessage.ValueToMessage

object ClientApp extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem             = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    import system.dispatcher

    val flow: Flow[Message, Message, NotUsed] = Flow.fromSinkAndSource(
      Sink.foreach(println),
      Source.single(Payload(MResponse(Hello("msuhtaq")), UUID.randomUUID()).textMessage)
    )

    val (upgradeResponse, closed) = Http().singleWebSocketRequest(WebSocketRequest("ws://localhost:5000/websocket"), flow)

    upgradeResponse.foreach(println)
  }
}
