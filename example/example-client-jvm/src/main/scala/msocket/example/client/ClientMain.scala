package msocket.example.client

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, ExampleStreamRequest}
import msocket.api.ContentType.Json
import msocket.http.post.HttpPostTransport
import msocket.http.sse.SseTransport
import msocket.http.ws.WebsocketTransport
import msocket.rsocket.client.RSocketTransportFactory

import scala.annotation.nowarn

/** Client can be wired with the preferred transport protocol */
object ClientMain extends ExampleCodecs {

  def main(args: Array[String]): Unit = {
    implicit lazy val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "demo")
    import system.executionContext

    val httpPort    = 5002
    val rSocketPort = 7002

    val PostEndpoint          = s"http://localhost:$httpPort/post-endpoint"
    val PostStreamingEndpoint = s"http://localhost:$httpPort/post-streaming-endpoint"
    val SseEndpoint           = s"http://localhost:$httpPort/sse-endpoint"
    val WebsocketEndpoint     = s"ws://localhost:$httpPort/websocket-endpoint"
    val RSocketEndpoint       = s"ws://localhost:$rSocketPort"

    lazy val httpResponseTransport = new HttpPostTransport[ExampleRequest](PostEndpoint, Json, () => None)
    lazy val httpStreamTransport   = new HttpPostTransport[ExampleStreamRequest](PostStreamingEndpoint, Json, () => None)

    lazy val (rSocketResponseTransport, _) = RSocketTransportFactory.connect[ExampleRequest](RSocketEndpoint, Json)
    lazy val (rSocketStreamTransport, _)   = RSocketTransportFactory.connect[ExampleStreamRequest](RSocketEndpoint, Json)

    @nowarn lazy val sseTransport       = new SseTransport[ExampleStreamRequest](SseEndpoint, Json, () => None)
    @nowarn lazy val websocketTransport = new WebsocketTransport[ExampleStreamRequest](WebsocketEndpoint, Json)

    val exampleClient = new ExampleClient(httpResponseTransport, httpStreamTransport)
    new ClientApp(exampleClient).testRun()
  }

}
