package msocket.simple.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.WebSocketRequest
import csw.simple.api.Codecs
import csw.simple.api.Protocol.{Hello, RequestResponse, RequestStream}
import msocket.core.api.Encoding
import msocket.core.api.Encoding.JsonText
import msocket.core.client.ClientSocket

object ClientApp extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    import system.dispatcher
    implicit val encoding: Encoding = JsonText

    val socket = new ClientSocket[RequestResponse, RequestStream](WebSocketRequest("ws://localhost:5000/websocket"))
    socket.requestResponse[String](Hello("msuhtaq")).onComplete(println)
    socket.requestResponse[String](Hello("msuhtaq1")).onComplete(println)
  }
}
