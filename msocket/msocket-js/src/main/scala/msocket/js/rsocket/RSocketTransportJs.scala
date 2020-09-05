package msocket.js.rsocket

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.models.ResponseHeaders
import msocket.api.{ContentEncoding, ErrorProtocol, Subscription}
import msocket.js.JsTransport
import msocket.portable.Observer
import typings.rsocketCore.anon.DataMimeType
import typings.rsocketCore.mod.RSocketClient
import typings.rsocketCore.rsocketclientMod.ClientConfig
import typings.rsocketCore.rsocketencodingMod.Encoders
import typings.rsocketFlowable.singleMod.{CancelCallback, IFutureSubscriber}
import typings.rsocketTypes.reactiveSocketTypesMod.{Payload, ReactiveSocket}
import typings.rsocketTypes.reactiveStreamTypesMod.{ISubscriber, ISubscription}
import typings.rsocketWebsocketClient.mod.{default => RSocketWebSocketClient}
import typings.rsocketWebsocketClient.rsocketwebsocketclientMod.ClientOptions

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.timers
import scala.scalajs.js.timers.SetIntervalHandle
import scala.util.{Failure, Success, Try}

class RSocketTransportJs[Req: Encoder: ErrorProtocol, En](uri: String, contentEncoding: ContentEncoding[En], encoders: Encoders[En])(
    implicit
    ec: ExecutionContext,
    streamingDelay: FiniteDuration
) extends JsTransport[Req] {

  private val client: RSocketClient[En, En] = new RSocketClient(
    ClientConfig(
      setup = DataMimeType(
        dataMimeType = contentEncoding.contentType.mimeType,
        keepAlive = 60000,
        lifetime = 1800000,
        metadataMimeType = contentEncoding.contentType.mimeType
      ),
      transport = new RSocketWebSocketClient(ClientOptions(url = uri), encoders)
    )
  )

  private def subscriber[T](p: Promise[T]): IFutureSubscriber[Try[T]] = {
    new IFutureSubscriber[Try[T]] {
      override def onComplete(value: Try[T]): Unit           = p.tryComplete(value)
      override def onError(error: js.Error): Unit            = p.tryFailure(new RuntimeException(error.message))
      override def onSubscribe(cancel: CancelCallback): Unit = println("inside onSubscribe")
    }
  }

  private val socketPromise: Promise[ReactiveSocket[En, En]] = Promise()
  client.connect().map(Try(_)).subscribe(PartialOf(subscriber(socketPromise)))

  override def requestResponse[Res: Decoder: Encoder](request: Req): Future[Res] = {
    val responsePromise: Promise[Res] = Promise()
    socketPromise.future.foreach { socket =>
      val payload = Payload[En, En]().setData(contentEncoding.encode(request)).setMetadata(contentEncoding.encode(ResponseHeaders()))
      socket
        .requestResponse(payload)
        .map { payload =>
          val headers = contentEncoding.decode[ResponseHeaders](payload.metadata.get)
          Try(contentEncoding.decodeFull(payload.data.get, headers.errorType))
        }
        .subscribe(PartialOf(subscriber(responsePromise)))
    }

    responsePromise.future
  }

  override def requestStream[Res: Decoder: Encoder](request: Req, observer: Observer[Res]): Subscription = {
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

    val subscriber: ISubscriber[Try[Res]] = {
      new ISubscriber[Try[Res]] {
        override def onComplete(): Unit                             = observer.onCompleted()
        override def onError(error: js.Error): Unit                 = observer.onError(new RuntimeException(error.message))
        override def onSubscribe(subscription: ISubscription): Unit = subscriptionPromise.trySuccess(subscription)
        override def onNext(value: Try[Res]): Unit                  =
          value match {
            case Failure(exception) => observer.onError(exception); cancelSubscription()
            case Success(value)     => observer.onNext(value)
          }
      }
    }

    socketPromise.future.foreach { socket =>
      socket
        .requestStream(Payload[En, En]().setData(contentEncoding.encode(request)).setMetadata(contentEncoding.encode(ResponseHeaders())))
        .map { payload =>
          val headers = contentEncoding.decode[ResponseHeaders](payload.metadata.get)
          Try(contentEncoding.decodeFull(payload.data.get, headers.errorType))
        }
        .subscribe(PartialOf(subscriber))
    }

    () =>
      cancelSubscription()
      observer.onCompleted()
  }

  def subscription(): Subscription = () => client.close()
}
