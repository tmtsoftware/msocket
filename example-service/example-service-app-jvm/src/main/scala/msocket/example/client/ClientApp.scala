package msocket.example.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import mscoket.impl.post.PostClient
import mscoket.impl.sse.SseClient
import mscoket.impl.ws.WebsocketClient

import concurrent.duration.DurationLong

object ClientApp extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem    = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    import system.dispatcher

    val postClient      = new PostClient[ExampleRequest](Uri("http://localhost:5000/post"))
    val websocketClient = new WebsocketClient[ExampleRequest]("ws://localhost:5000/websocket")
    val sseClient       = new SseClient[ExampleRequest]("http://localhost:5000/sse")

    val exampleClient = new ExampleClient(postClient)

//    exampleClient.getNumbers(3).mapMaterializedValue(_.onComplete(println)).runForeach(println)
//    exampleClient.getNames(5).runForeach(println)

//    Thread.sleep(2000)
    exampleClient.hello("msuhtaq").onComplete(println)
    exampleClient.helloStream("mushtaq").throttle(1, 1.second).runForeach(println)
//    exampleClient.hello("msuhtaq1").onComplete(println)
//    exampleClient.square(3).onComplete(println)
//    exampleClient.square(4).onComplete(println)
  }
}
