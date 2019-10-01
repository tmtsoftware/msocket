package msocket.impl.streaming

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Json}
import msocket.api.models.{Result, StreamError, StreamStarted, StreamStatus, Subscription}

import scala.concurrent.{Future, Promise}

abstract class ConnectedSource[Res, Mat] extends Source[Res, Mat] {
  def onTextMessage(res: String): Unit
  var onMessage: Res => Unit     = x => ()
  var subscription: Subscription = () => ()
}

class PlainConnectedSource[Res: Decoder] extends ConnectedSource[Res, NotUsed] {
  override def onTextMessage(res: String): Unit = {
    onMessage(Json.decode(res.getBytes()).to[Res].value)
  }

  override val mat: NotUsed = NotUsed
}

class ConnectedSourceWithErr[Res: Decoder] extends ConnectedSource[Res, Future[StreamStatus]] {
  private val matPromise: Promise[StreamStatus] = Promise()

  override def onTextMessage(res: String): Unit = {
    val response = Json.decode(res.getBytes()).to[Result[Res, StreamError]].value
    response match {
      case Result.Success(value) => onMessage(value); matPromise.trySuccess(StreamStarted(() => subscription.cancel()))
      case Result.Error(error)   => matPromise.trySuccess(error); subscription.cancel()
    }
  }

  override val mat: Future[StreamStatus] = matPromise.future
}
