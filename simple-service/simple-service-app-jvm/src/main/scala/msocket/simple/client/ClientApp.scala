package msocket.simple.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import csw.simple.api.client.SimpleClient
import csw.simple.api.{Codecs, WebsocketRequest}
import mscoket.impl.Encoding.JsonText
import mscoket.impl.{PostClientJvm, WebsocketClientJvm}
import msocket.api.{PostClient, WebsocketClient}

object ClientApp extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem    = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    import system.dispatcher

    val websocketClient: WebsocketClient[WebsocketRequest] =
      new WebsocketClientJvm[WebsocketRequest]("ws://localhost:5000/websocket", JsonText)

    val postClient: PostClient = new PostClientJvm(Uri("http://localhost:5000/post"))
    val simpleClient           = new SimpleClient(websocketClient, postClient)

    simpleClient.getNumbers(3).mapMaterializedValue(_.onComplete(println)).runForeach(println)
//    simpleClient.getNames(5).runForeach(println)

//    Thread.sleep(2000)
    simpleClient.hello("msuhtaq").onComplete(println)
//    simpleClient.hello("msuhtaq1").onComplete(println)
//    simpleClient.square(3).onComplete(println)
//    simpleClient.square(4).onComplete(println)
  }
}
