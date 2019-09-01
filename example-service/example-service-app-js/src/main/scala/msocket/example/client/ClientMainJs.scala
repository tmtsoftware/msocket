package msocket.example.client

import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import msocket.impl.post.PostClientJs
import msocket.impl.sse.SseClientJs
import msocket.impl.ws.WebsocketClientJs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationLong, FiniteDuration}

object ClientMainJs extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val streamingDelay: FiniteDuration = 1.second

    val websocketClient = new WebsocketClientJs[ExampleRequest]("ws://localhost:5000/websocket")
    val sseClient       = new SseClientJs[ExampleRequest]("http://localhost:5000/sse")
    val postClient      = new PostClientJs[ExampleRequest]("http://localhost:5000/post")

    val exampleClient = new ExampleClient(postClient)
    new ClientAppJs(exampleClient).testRun()
  }
}
