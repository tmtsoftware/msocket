package msocket.example.client

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleRequest.{ExampleRequestResponse, ExampleRequestStream}
import msocket.api.ContentType.Json
import msocket.impl.post.HttpPostTransport
import msocket.impl.rsocket.client.RSocketTransportFactory
import msocket.impl.sse.SseTransport
import msocket.impl.ws.WebsocketTransport

/** Client can be wired with the preferred transport protocol */
object ClientMain extends ExampleCodecs {

  def main(args: Array[String]): Unit = {
    implicit lazy val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "demo")
    import system.executionContext

    val PostEndpoint      = "http://localhost:5000/post-endpoint"
    val SseEndpoint       = "http://localhost:5000/sse-endpoint"
    val WebsocketEndpoint = "ws://localhost:5000/websocket-endpoint"
    val RSocketEndpoint   = "ws://localhost:7000"

    lazy val httpResponseTransport = new HttpPostTransport[ExampleRequestResponse](PostEndpoint, Json, () => None)
    lazy val httpStreamTransport   = new HttpPostTransport[ExampleRequestStream](PostEndpoint, Json, () => None)

    lazy val rSocketResponseTransport = new RSocketTransportFactory[ExampleRequestResponse].transport(RSocketEndpoint, Json)
    lazy val rSocketStreamTransport   = new RSocketTransportFactory[ExampleRequestStream].transport(RSocketEndpoint, Json)

    lazy val sseTransport       = new SseTransport[ExampleRequestStream](SseEndpoint)
    lazy val websocketTransport = new WebsocketTransport[ExampleRequestStream](WebsocketEndpoint, Json)

    val exampleClient = new ExampleClient(httpResponseTransport, httpStreamTransport)
    new ClientApp(exampleClient).testRun()
  }

}
