package msocket.example.client

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import caseapp.{CommandApp, RemainingArgs}
import csw.aas.installed.api.InstalledAppAuthAdapter
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import msocket.example.client.CliCommand._
import msocket.impl.Encoding.JsonText
import msocket.impl.post.HttpPostTransport
import msocket.impl.rsocket.client.RSocketTransportFactory
import msocket.impl.sse.SseTransport
import msocket.impl.ws.WebsocketTransport
import pprint.{pprintln => println}

object CliMain extends CommandApp[CliCommand] with Codecs {

  implicit lazy val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "cli")
  import system.executionContext
  lazy val adapter: InstalledAppAuthAdapter = AdapterFactory.makeAdapter(system)

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
        lazy val httpPostTransport =
          new HttpPostTransport[ExampleRequest]("http://localhost:5000/post", JsonText, () => adapter.getAccessToken().map(_.value))
        lazy val sseTransport       = new SseTransport[ExampleRequest]("http://localhost:5000/sse")
        lazy val websocketTransport = new WebsocketTransport[ExampleRequest]("ws://localhost:5000/websocket", JsonText)
        lazy val rSocketTransport   = new RSocketTransportFactory[ExampleRequest].transport("ws://localhost:7000")

        val exampleClient = new ExampleClient(httpPostTransport)
        new ClientApp(exampleClient).testRun()
        Thread.sleep(3000)
        system.terminate()
    }
  }
}
