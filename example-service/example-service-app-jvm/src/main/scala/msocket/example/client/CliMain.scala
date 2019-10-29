package msocket.example.client

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import caseapp.{CommandApp, RemainingArgs}
import csw.aas.installed.api.InstalledAppAuthAdapter
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import io.bullet.borer.Json
import mscoket.impl.post.HttpPostTransport
import mscoket.impl.rsocket.client.RSocketTransportFactory
import mscoket.impl.sse.SseTransport
import mscoket.impl.ws.WebsocketTransport
import msocket.example.client.CliCommand._
import pprint.{pprintln => println}

object CliMain extends CommandApp[CliCommand] with Codecs {

  implicit lazy val system: ActorSystem = ActorSystem()
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
        lazy val httpPostTransport =
          new HttpPostTransport[ExampleRequest]("http://localhost:5000/post", Json, () => adapter.getAccessToken().map(_.value))
        lazy val sseTransport       = new SseTransport[ExampleRequest]("http://localhost:5000/sse")
        lazy val websocketTransport = new WebsocketTransport[ExampleRequest]("ws://localhost:5000/websocket")
        lazy val rSocketTransport   = new RSocketTransportFactory[ExampleRequest].transport("ws://localhost:7000")

        val exampleClient = new ExampleClient(httpPostTransport)
        new ClientApp(exampleClient).testRun()
        Thread.sleep(3000)
        system.terminate()
    }
  }
}
