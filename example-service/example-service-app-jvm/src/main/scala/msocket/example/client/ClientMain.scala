package msocket.example.client

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{ExampleCodecs, ExampleRequest}
import msocket.api.ContentType.{Cbor, Json}
import msocket.impl.post.HttpPostTransport
import msocket.impl.rsocket.client.RSocketTransportFactory
import msocket.impl.sse.SseTransport
import msocket.impl.ws.WebsocketTransport

object ClientMain extends ExampleCodecs {

  def main(args: Array[String]): Unit = {
    implicit lazy val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "demo")
    import system.executionContext

    lazy val httpPostTransport =
      new HttpPostTransport[ExampleRequest]("http://localhost:5000/post-endpoint", Cbor, () => None).logRequest().logRequestResponse()
    lazy val sseTransport = new SseTransport[ExampleRequest]("http://localhost:5000/sse-endpoint").logRequestResponse()
    lazy val websocketTransport =
      new WebsocketTransport[ExampleRequest]("ws://localhost:5000/websocket-endpoint", Json).logRequest().logRequestResponse()
    lazy val rSocketTransport = new RSocketTransportFactory[ExampleRequest].transport("ws://localhost:7000", Json).logRequestResponse()

    val exampleClient = new ExampleClient(httpPostTransport)
    new ClientApp(exampleClient).testRun()
  }

}
