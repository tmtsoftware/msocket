package msocket.impl.rsocket

import java.nio.ByteBuffer

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.CborByteBuffer
import msocket.api.{ErrorProtocol, Subscription}
import msocket.impl.streaming.Connector
import typings.rsocketDashCore.Anon_DataMimeType
import typings.rsocketDashCore.rSocketClientMod.ClientConfig
import typings.rsocketDashCore.rsocketDashCoreMod.RSocketClient
import typings.rsocketDashFlowable.singleMod.{CancelCallback, IFutureSubscriber}
import typings.rsocketDashTypes.reactiveSocketTypesMod.{Payload, ReactiveSocket}
import typings.rsocketDashWebsocketDashClient.rSocketWebSocketClientMod.ClientOptions
import typings.rsocketDashWebsocketDashClient.rsocketDashWebsocketDashClientMod.{default => RSocketWebSocketClient}
import typings.std

import scala.concurrent.{ExecutionContext, Promise}

class RSocketConnector[Req: Encoder: ErrorProtocol](uri: String)(implicit ec: ExecutionContext) extends Connector[Req] {

  private val client: RSocketClient[ByteBuffer, ByteBuffer] = new RSocketClient(
    ClientConfig(
      setup = Anon_DataMimeType(
        dataMimeType = "application/cbor",
        keepAlive = 60000,
        lifetime = 1800000,
        metadataMimeType = "application/cbor"
      ),
      transport = new RSocketWebSocketClient(ClientOptions(url = uri))
    )
  )

  private val socketPromise: Promise[ReactiveSocket[ByteBuffer, ByteBuffer]] = Promise()

  private val subscriber = new IFutureSubscriber[ReactiveSocket[ByteBuffer, ByteBuffer]] {
    def onComplete(socket: ReactiveSocket[ByteBuffer, ByteBuffer]): Unit = socketPromise.trySuccess(socket)
    def onError(error: std.Error): Unit                                  = println(error.stack)
    def onSubscribe(cancel: CancelCallback): Unit                        = println("inside onSubscribe")
  }

  override def connect[Res: Decoder](req: Req, onMessage: Res => Unit): Subscription = {
    socketPromise.future.foreach { socket =>
      socket
        .requestStream(Payload(CborByteBuffer.encode(req)))
        .subscribe((payload: Payload[ByteBuffer, ByteBuffer]) => onMessage(CborByteBuffer.decodeWithError(payload.data.get)))
    }

    client.connect().subscribe(PartialOf(subscriber))
    () => client.close()
  }
}
