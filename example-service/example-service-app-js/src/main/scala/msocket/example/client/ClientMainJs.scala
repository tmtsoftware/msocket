package msocket.example.client

import akka.actor.typed.ActorSystem
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import msocket.impl.post.PostTransportJs
import msocket.impl.rsocket.RSocketTransportJs
import msocket.impl.sse.SseTransportJs
import msocket.impl.ws.WebsocketTransportJs

import scala.concurrent.ExecutionContext.Implicits.global
import com.github.ghik.silencer.silent

import scala.concurrent.duration.{DurationLong, FiniteDuration}

object ClientMainJs extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val streamingDelay: FiniteDuration = 1.second
    implicit val actorSystem: ActorSystem[Any]  = new ActorSystem

    @silent lazy val postTransport      = new PostTransportJs[ExampleRequest]("http://localhost:5000/post-endpoint")
    @silent lazy val sseTransport       = new SseTransportJs[ExampleRequest]("http://localhost:5000/sse-endpoint")
    @silent lazy val websocketTransport = new WebsocketTransportJs[ExampleRequest]("ws://localhost:5000/websocket-endpoint")
    lazy val rSocketTransport           = new RSocketTransportJs[ExampleRequest]("ws://localhost:7000")

    val exampleClient = new ExampleClient(rSocketTransport)
    new ClientAppJs(exampleClient).testRun()
  }
}
