package msocket.impl.streaming

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Json}
import msocket.api.models._

import scala.concurrent.{Future, Promise}

abstract class ConnectedSource[Res, Mat] extends Source[Res, Mat] {
  def onTextMessage(res: String): Unit
  protected var onMessage: Res => Unit = x => ()
  var subscription: Subscription       = () => ()
  def runForeach(f: Res => Unit): Unit = {
    onMessage = f
  }
}

class PlainConnectedSource[Res: Decoder] extends ConnectedSource[Res, Subscription] {
  override def onTextMessage(res: String): Unit = {
    onMessage(Json.decode(res.getBytes()).to[Res].value)
  }

  override val materializedValue: Subscription = subscription
}

class ConnectedSourceWithStatus[Res: Decoder] extends ConnectedSource[Res, Future[StreamStatus]] {
  private val matPromise: Promise[StreamStatus] = Promise()

  override def onTextMessage(res: String): Unit = {
    val response = Json.decode(res.getBytes()).to[Result[Res, StreamError]].value
    response match {
      case Result.Success(value) => onMessage(value); matPromise.trySuccess(StreamStarted(subscription))
      case Result.Error(error)   => matPromise.trySuccess(error); subscription.cancel()
    }
  }

  override val materializedValue: Future[StreamStatus] = matPromise.future
}
