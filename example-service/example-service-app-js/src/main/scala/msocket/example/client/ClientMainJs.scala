package msocket.example.client

import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import msocket.impl.post.PostClientJs
import msocket.impl.rsocket.RSocketClientJs
import msocket.impl.sse.SseClientJs
import msocket.impl.ws.WebsocketClientJs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationLong, FiniteDuration}

object ClientMainJs extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val streamingDelay: FiniteDuration = 1.second

    lazy val postClient      = new PostClientJs[ExampleRequest]("http://localhost:5000/post")
    lazy val sseClient       = new SseClientJs[ExampleRequest]("http://localhost:5000/sse")
    lazy val websocketClient = new WebsocketClientJs[ExampleRequest]("ws://localhost:5000/websocket")
    lazy val rSocketClient   = new RSocketClientJs[ExampleRequest]("ws://localhost:7000")

    val exampleClient = new ExampleClient(rSocketClient)
    new ClientAppJs(exampleClient).testRun()
  }
}
