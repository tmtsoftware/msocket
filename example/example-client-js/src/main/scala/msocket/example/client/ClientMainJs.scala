package msocket.example.client

import akka.actor.typed.ActorSystem
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, ExampleStreamRequest}
import msocket.api.ContentType.{Cbor, Json}
import msocket.js.post.HttpPostTransportJs
import msocket.js.rsocket.RSocketTransportFactoryJs
import msocket.js.sse.SseTransportJs
import msocket.js.ws.WebsocketTransportJs

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationLong, FiniteDuration}

/** ScalaJS based client can be wired with the preferred transport protocol */
object ClientMainJs extends ExampleCodecs {

  def main(args: Array[String]): Unit = {
    implicit val streamingDelay: FiniteDuration = 1.second
    implicit val actorSystem: ActorSystem[Any]  = new ActorSystem

    val httpPort    = 5002
    val rSocketPort = 7002

    val PostEndpoint          = s"http://localhost:$httpPort/post-endpoint"
    val PostEndpoint2         = s"http://localhost:$httpPort/post-endpoint2"
    val PostStreamingEndpoint = s"http://localhost:$httpPort/post-streaming-endpoint"
    val SseEndpoint           = s"http://localhost:$httpPort/sse-endpoint"
    val WebsocketEndpoint     = s"ws://localhost:$httpPort/websocket-endpoint"
    val RSocketEndpoint       = s"ws://localhost:$rSocketPort"

    lazy val httpResponseTransport          = new HttpPostTransportJs[ExampleRequest](PostEndpoint, Json)
    @nowarn lazy val httpResponseTransport2 = new HttpPostTransportJs[ExampleRequest](PostEndpoint2, Json)
    @nowarn lazy val httpStreamTransport    = new HttpPostTransportJs[ExampleStreamRequest](PostStreamingEndpoint, Json)

    lazy val (rSocketResponseTransport, _) = RSocketTransportFactoryJs.connect[ExampleRequest](RSocketEndpoint, Cbor)
    lazy val (rSocketStreamTransport, _)   = RSocketTransportFactoryJs.connect[ExampleStreamRequest](RSocketEndpoint, Json)

    @nowarn lazy val sseTransport = new SseTransportJs[ExampleStreamRequest](SseEndpoint)
    lazy val websocketTransport   = new WebsocketTransportJs[ExampleStreamRequest](WebsocketEndpoint, Json)

    val exampleClient = new ExampleClient(httpResponseTransport, websocketTransport)
    new ClientAppJs(exampleClient).testRun()
  }
}
