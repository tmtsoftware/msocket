package msocket.example.client

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps
import akka.stream.ActorMaterializer
import caseapp.{CommandApp, RemainingArgs}
import csw.aas.installed.api.InstalledAppAuthAdapter
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import mscoket.impl.post.PostClient
import mscoket.impl.rsocket.client.RSocketClientFactory
import mscoket.impl.sse.SseClient
import mscoket.impl.ws.WebsocketClient
import msocket.example.client.CliCommand._
import pprint.{pprintln => println}

object ClientMain extends CommandApp[CliCommand] with Codecs {

  implicit lazy val system: ActorSystem    = ActorSystem()
  lazy implicit val mat: ActorMaterializer = ActorMaterializer()
  import system.dispatcher
  lazy val adapter: InstalledAppAuthAdapter = AdapterFactory.makeAdapter(system.toTyped)

  def run(command: CliCommand, args: RemainingArgs): Unit = {
    command match {
      case Login() =>
        adapter.login()
        system.terminate()
      case Logout() =>
        adapter.logout()
        system.terminate()
      case MakeCall() =>
        println(adapter.getAccessToken())
        lazy val postClient      = new PostClient[ExampleRequest]("http://localhost:5000/post", adapter.getAccessTokenString())
        lazy val sseClient       = new SseClient[ExampleRequest]("http://localhost:5000/sse")
        lazy val websocketClient = new WebsocketClient[ExampleRequest]("ws://localhost:5000/websocket")
        lazy val rSocketClient   = new RSocketClientFactory[ExampleRequest].client("ws://localhost:7000")

        val exampleClient = new ExampleClient(postClient)
        new ClientApp(exampleClient).testRun()
        Thread.sleep(3000)
        system.terminate()
    }
  }
}
