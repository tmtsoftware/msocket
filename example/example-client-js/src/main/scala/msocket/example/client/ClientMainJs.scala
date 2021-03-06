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

    val PostEndpoint          = "http://localhost:5000/post-endpoint"
    val PostEndpoint2         = "http://localhost:5000/post-endpoint2"
    val PostStreamingEndpoint = "http://localhost:5000/post-streaming-endpoint"
    val SseEndpoint           = "http://localhost:5000/sse-endpoint"
    val WebsocketEndpoint     = "ws://localhost:5000/websocket-endpoint"
    val RSocketEndpoint       = "ws://localhost:7000"

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
