package msocket.example.client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import mscoket.impl.post.PostClient
import mscoket.impl.rsocket.client.RSocketClientFactory
import mscoket.impl.sse.SseClient
import mscoket.impl.ws.WebsocketClient

object ClientMain extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem    = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    import system.dispatcher

    lazy val postClient      = new PostClient[ExampleRequest]("http://localhost:5000/post")
    lazy val sseClient       = new SseClient[ExampleRequest]("http://localhost:5000/sse")
    lazy val websocketClient = new WebsocketClient[ExampleRequest]("ws://localhost:5000/websocket")
    lazy val rSocketClient   = new RSocketClientFactory[ExampleRequest].client("ws://localhost:7000")

    val exampleClient = new ExampleClient(rSocketClient)
    new ClientApp(exampleClient).testRun()
  }

}
