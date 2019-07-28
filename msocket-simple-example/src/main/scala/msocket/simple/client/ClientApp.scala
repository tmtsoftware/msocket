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
import msocket.core.api.Encoding.JsonText
import msocket.core.api.{Encoding, Response, Payload}

object ClientApp extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem             = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    import system.dispatcher
    implicit val encoding: Encoding.JsonText.type = JsonText

    val flow: Flow[Message, Message, NotUsed] = Flow.fromSinkAndSource(
      Sink.foreach(println),
      Source.single(encoding.strict(Payload(Response(Hello("msuhtaq")), UUID.randomUUID())))
    )

    val (upgradeResponse, closed) = Http().singleWebSocketRequest(WebSocketRequest("ws://localhost:5000/websocket"), flow)

    upgradeResponse.foreach(println)
  }
}
