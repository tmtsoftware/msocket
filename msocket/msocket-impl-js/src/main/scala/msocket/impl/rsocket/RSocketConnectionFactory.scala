package msocket.impl.rsocket

import io.bullet.borer.{Encoder, Json}
import msocket.impl.streaming.{ConnectedSource, ConnectionFactory}
import typings.rsocketDashCore.Anon_DataMimeType
import typings.rsocketDashCore.rSocketClientMod.ClientConfig
import typings.rsocketDashCore.rsocketDashCoreMod.RSocketClient
import typings.rsocketDashFlowable.singleMod.{CancelCallback, IFutureSubscriber}
import typings.rsocketDashTypes.reactiveSocketTypesMod.{Payload, ReactiveSocket}
import typings.rsocketDashWebsocketDashClient.rSocketWebSocketClientMod.ClientOptions
import typings.rsocketDashWebsocketDashClient.rsocketDashWebsocketDashClientMod.{default => RSocketWebSocketClient}
import typings.std

import scala.concurrent.{ExecutionContext, Promise}

class RSocketConnectionFactory[Req: Encoder](uri: String)(implicit ec: ExecutionContext) extends ConnectionFactory {

  private val client: RSocketClient[String, String] = new RSocketClient(
    ClientConfig(
      setup = new Anon_DataMimeType {
        override var dataMimeType: String     = "application/json"
        override var keepAlive: Double        = 60000
        override var lifetime: Double         = 1800000
        override var metadataMimeType: String = "application/json"
      },
      transport = new RSocketWebSocketClient(ClientOptions(url = uri))
    )
  )

  private val socketPromise: Promise[ReactiveSocket[String, String]] = Promise()

  private val subscriber = new IFutureSubscriber[ReactiveSocket[String, String]] {
    def onComplete(socket: ReactiveSocket[String, String]): Unit = socketPromise.trySuccess(socket)
    def onError(error: std.Error): Unit                          = println(error.stack)
    def onSubscribe(cancel: CancelCallback): Unit                = println("inside onSubscribe")
  }

  client.connect().subscribe(PartialFactory.from(subscriber))

  def connect[S <: ConnectedSource[_, _]](req: Req, source: S): S = {
    socketPromise.future.foreach { socket =>
      socket
        .requestStream(Payload(Json.encode(req).toUtf8String))
        .subscribe(payload => source.onTextMessage(payload.data.get))
    }

    source
  }
}
