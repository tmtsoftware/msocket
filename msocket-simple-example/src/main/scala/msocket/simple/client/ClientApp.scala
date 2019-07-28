package msocket.simple.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.WebSocketRequest
import csw.simple.api.Codecs
import csw.simple.api.Protocol.{RequestResponse, RequestStream}
import msocket.core.api.Encoding
import msocket.core.api.Encoding.JsonText
import msocket.core.client.{ClientSocket, ClientSocketImpl}

object ClientApp extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    import system.dispatcher
    implicit val encoding: Encoding = JsonText

    val socket: ClientSocket[RequestResponse, RequestStream] = {
      new ClientSocketImpl[RequestResponse, RequestStream](WebSocketRequest("ws://localhost:5000/websocket"))
    }

    val client = new SimpleClient(socket)

    client.hello("msuhtaq").onComplete(println)
//    Thread.sleep(1000)
    client.hello("msuhtaq1").onComplete(println)
//    Thread.sleep(1000)
    client.square(3).onComplete(println)
//    Thread.sleep(1000)
    client.square(4).onComplete(println)
//    Thread.sleep(1000)
    client.square(5).onComplete(println)
    Thread.sleep(1000)
    client.square(6).onComplete(println)
    Thread.sleep(1000)
    //    client.square(9).onComplete(println)
  }
}
