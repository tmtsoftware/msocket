package msocket.impl.rsocket

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.Encoding.JsonText
import msocket.api.{ErrorProtocol, Subscription}
import msocket.impl.JsTransport
import typings.rsocketDashCore.Anon_DataMimeType
import typings.rsocketDashCore.rSocketClientMod.ClientConfig
import typings.rsocketDashCore.rsocketDashCoreMod.RSocketClient
import typings.rsocketDashFlowable.singleMod.{CancelCallback, IFutureSubscriber}
import typings.rsocketDashTypes.reactiveSocketTypesMod.{Payload, ReactiveSocket}
import typings.rsocketDashTypes.reactiveStreamTypesMod.{ISubscriber, ISubscription}
import typings.rsocketDashWebsocketDashClient.rSocketWebSocketClientMod.ClientOptions
import typings.rsocketDashWebsocketDashClient.rsocketDashWebsocketDashClientMod.{default => RSocketWebSocketClient}
import typings.std

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

class RSocketTransportJs[Req: Encoder: ErrorProtocol](uri: String)(implicit ec: ExecutionContext) extends JsTransport[Req] {

  private val client: RSocketClient[String, String] = new RSocketClient(
    ClientConfig(
      setup = Anon_DataMimeType(
        dataMimeType = "application/json",
        keepAlive = 60000,
        lifetime = 1800000,
        metadataMimeType = "application/json"
      ),
      transport = new RSocketWebSocketClient(ClientOptions(url = uri))
    )
  )

  private def subscriber[T](p: Promise[T]): IFutureSubscriber[T] = new IFutureSubscriber[T] {
    def onComplete(value: T): Unit                = p.trySuccess(value)
    def onError(error: std.Error): Unit           = p.tryFailure(new RuntimeException(error.stack.get))
    def onSubscribe(cancel: CancelCallback): Unit = println("inside onSubscribe")
  }

  private val socketPromise: Promise[ReactiveSocket[String, String]] = Promise()
  client.connect().subscribe(PartialOf(subscriber(socketPromise)))

  override def requestResponse[Res: Decoder: Encoder](req: Req): Future[Res] = {
    val responsePromise: Promise[Res] = Promise()
    socketPromise.future.foreach { socket =>
      socket
        .requestResponse(Payload(JsonText.encode(req)))
        .map(payload => JsonText.decodeWithError(payload.data.get))
        .subscribe(PartialOf(subscriber(responsePromise)))
    }

    responsePromise.future
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Res => Unit, onError: Throwable => Unit): Subscription = {
    val subscriptionPromise: Promise[ISubscription] = Promise()

    val subscriber = new ISubscriber[Res] {
      override def onComplete(): Unit                             = println("stream completed")
      override def onError(error: std.Error): Unit                = println(("stream errored out", error.name, error.message, error.stack))
      override def onNext(value: Res): Unit                       = onMessage(value)
      override def onSubscribe(subscription: ISubscription): Unit = subscriptionPromise.trySuccess(subscription)
    }

    socketPromise.future.foreach { socket =>
      socket
        .requestStream(Payload(JsonText.encode(request)))
        .map { payload =>
          try JsonText.decodeWithError(payload.data.get)
          catch {
            case NonFatal(ex) => onError(ex); subscriptionPromise.future.map(_.cancel())
          }

        }
        .subscribe(PartialOf(subscriber))
    }

    () => subscriptionPromise.future.map(_.cancel())
  }

  def shutdown(): Unit = client.close()
}
