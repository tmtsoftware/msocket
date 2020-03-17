package msocket.impl.rsocket

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{ContentType, ErrorProtocol, Subscription}
import msocket.impl.JsTransport
import typings.rsocketCore.AnonDataMimeType
import typings.rsocketCore.mod.RSocketClient
import typings.rsocketCore.rsocketclientMod.ClientConfig
import typings.rsocketFlowable.singleMod.IFutureSubscriber
import typings.rsocketTypes.reactiveSocketTypesMod.{Payload, ReactiveSocket}
import typings.rsocketTypes.reactiveStreamTypesMod.{ISubscriber, ISubscription}
import typings.rsocketWebsocketClient.mod.{default => RSocketWebSocketClient}
import typings.rsocketWebsocketClient.rsocketwebsocketclientMod.ClientOptions

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js.timers
import scala.scalajs.js.timers.SetIntervalHandle
import scala.util.{Failure, Success, Try}

class RSocketTransportJs[Req: Encoder: ErrorProtocol, CT <: ContentType](uri: String)(
    implicit rSocketEncoders: RSocketEncoders[CT],
    ec: ExecutionContext,
    streamingDelay: FiniteDuration
) extends JsTransport[Req] {

  import rSocketEncoders._

  private val client: RSocketClient[rSocketEncoders.En, Null] = new RSocketClient(
    ClientConfig(
      setup = AnonDataMimeType(
        dataMimeType = contentEncoding.contentType.mimeType,
        keepAlive = 60000,
        lifetime = 1800000,
        metadataMimeType = contentEncoding.contentType.mimeType
      ),
      transport = new RSocketWebSocketClient(ClientOptions(url = uri), encoders)
    )
  )

  private def subscriber[T](p: Promise[T]): IFutureSubscriber[Try[T]] = IFutureSubscriber[Try[T]](
    p.tryComplete,
    e => p.tryFailure(new RuntimeException(e.message)),
    _ => println("inside onSubscribe")
  )

  private val socketPromise: Promise[ReactiveSocket[rSocketEncoders.En, Null]] = Promise()
  client.connect().map(Try(_)).subscribe(PartialOf(subscriber(socketPromise)))

  override def requestResponse[Res: Decoder: Encoder](req: Req): Future[Res] = {
    val responsePromise: Promise[Res] = Promise()
    socketPromise.future.foreach { socket =>
      socket
        .requestResponse(Payload(contentEncoding.encode(req)))
        .map(payload => Try(contentEncoding.decodeWithError(payload.data.get)))
        .subscribe(PartialOf(subscriber(responsePromise)))
    }

    responsePromise.future
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, onMessage: Res => Unit, onError: Throwable => Unit): Subscription = {
    val subscriptionPromise: Promise[ISubscription] = Promise()

    val pullStreamHandle: Future[SetIntervalHandle] = subscriptionPromise.future.map { subscription =>
      timers.setInterval(streamingDelay) {
        subscription.request(1)
      }
    }

    def cancelSubscription(): Unit = {
      subscriptionPromise.future.foreach(_.cancel())
      pullStreamHandle.foreach(timers.clearInterval)
    }

    val subscriber: ISubscriber[Try[Res]] = ISubscriber[Try[Res]](
      () => println("stream completed"),
      e => onError(new RuntimeException(e.message)), {
        case Failure(exception) =>
          onError(exception)
          cancelSubscription()
        case Success(value) => onMessage(value)
      },
      subscriptionPromise.trySuccess
    )

    socketPromise.future.foreach { socket =>
      socket
        .requestStream(Payload(contentEncoding.encode(request)))
        .map(payload => Try(contentEncoding.decodeWithError(payload.data.get)))
        .subscribe(PartialOf(subscriber))
    }

    () => cancelSubscription()
  }

  def shutdown(): Unit = client.close()
}
