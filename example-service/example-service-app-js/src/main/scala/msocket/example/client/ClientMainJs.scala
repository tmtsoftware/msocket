package msocket.example.client

import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import msocket.impl.post.PostTransportJs
import msocket.impl.rsocket.RSocketTransportJs
import msocket.impl.sse.SseTransportJs
import msocket.impl.ws.WebsocketTransportJs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationLong, FiniteDuration}

object ClientMainJs extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val streamingDelay: FiniteDuration = 1.second

    lazy val postTransport      = new PostTransportJs[ExampleRequest]("http://localhost:5000/post")
    lazy val sseTransport       = new SseTransportJs[ExampleRequest]("http://localhost:5000/sse")
    lazy val websocketTransport = new WebsocketTransportJs[ExampleRequest]("ws://localhost:5000/websocket")
    lazy val rSocketTransport   = new RSocketTransportJs[ExampleRequest]("ws://localhost:7000")

    val exampleClient = new ExampleClient(rSocketTransport)
    new ClientAppJs(exampleClient).testRun()
  }
}
