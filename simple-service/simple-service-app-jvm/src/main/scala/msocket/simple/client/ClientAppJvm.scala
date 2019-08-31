package msocket.simple.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import csw.simple.api.client.SimpleClient
import csw.simple.api.{Codecs, SimpleRequest}
import mscoket.impl.post.PostClientJvm
import mscoket.impl.sse.SseClientJvm
import mscoket.impl.ws.WebsocketClientJvm
import concurrent.duration.DurationLong

object ClientAppJvm extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem    = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    import system.dispatcher

    val postClient      = new PostClientJvm[SimpleRequest](Uri("http://localhost:5000/post"))
    val websocketClient = new WebsocketClientJvm[SimpleRequest]("ws://localhost:5000/websocket")
    val sseClient       = new SseClientJvm[SimpleRequest]("http://localhost:5000/sse")

    val simpleClient = new SimpleClient(postClient)

//    simpleClient.getNumbers(3).mapMaterializedValue(_.onComplete(println)).runForeach(println)
//    simpleClient.getNames(5).runForeach(println)

//    Thread.sleep(2000)
    simpleClient.hello("msuhtaq").onComplete(println)
    simpleClient.helloStream("mushtaq").throttle(1, 1.second).runForeach(println)
//    simpleClient.hello("msuhtaq1").onComplete(println)
//    simpleClient.square(3).onComplete(println)
//    simpleClient.square(4).onComplete(println)
  }
}
