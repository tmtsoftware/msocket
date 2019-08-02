package msocket.simple.client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import csw.simple.api.{Codecs, RequestProtocol}
import msocket.core.api.Encoding.JsonText
import msocket.core.client.{ClientSocket, ClientSocketImpl}

object ClientApp extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem    = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    import system.dispatcher

    val socket: ClientSocket[RequestProtocol] = new ClientSocketImpl[RequestProtocol]("ws://localhost:5000/websocket", JsonText)

    val client = new SimpleClient(socket)

    client.getNumbers(3).runForeach(println)
//    client.getNames(5).runForeach(println)

//    Thread.sleep(2000)
    client.hello("msuhtaq").onComplete(println)
//    client.hello("msuhtaq1").onComplete(println)
//    client.square(3).onComplete(println)
//    client.square(4).onComplete(println)
  }
}
