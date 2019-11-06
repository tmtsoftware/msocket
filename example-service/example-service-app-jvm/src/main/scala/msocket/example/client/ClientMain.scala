package msocket.example.client

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import io.bullet.borer.{Encoder, Json}
import msocket.impl.Encoding.JsonText
import msocket.impl.post.HttpPostTransport
import msocket.impl.rsocket.client.RSocketTransportFactory
import msocket.impl.sse.SseTransport
import msocket.impl.ws.WebsocketTransport

object ClientMain extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit lazy val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "demo")
    import system.executionContext

    def action[Req: Encoder](req: Req): Unit = println(Json.encode(req).toUtf8String)

    lazy val httpPostTransport =
      new HttpPostTransport[ExampleRequest]("http://localhost:5000/post-endpoint", JsonText, () => None).interceptRequest(action)
    lazy val sseTransport = new SseTransport[ExampleRequest]("http://localhost:5000/sse-endpoint")
    lazy val websocketTransport =
      new WebsocketTransport[ExampleRequest]("ws://localhost:5000/websocket-endpoint", JsonText).interceptRequest(action)
    lazy val rSocketTransport = new RSocketTransportFactory[ExampleRequest].transport("ws://localhost:7000")

    val exampleClient = new ExampleClient(websocketTransport)
    new ClientApp(exampleClient).testRun()
  }

}
