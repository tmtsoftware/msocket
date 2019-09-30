package msocket.impl.streaming

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Json}
import msocket.api.utils.Result
import msocket.api.{StreamStatus, StreamStarted}

import scala.concurrent.{Future, Promise}

abstract class ConnectedSource[Res, Mat] extends Source[Res, Mat] {
  def onTextMessage(res: String): Unit
  var onMessage: Res => Unit = x => ()
  var closeable: Closeable = new Closeable {
    override def closeStream(): Unit = ()
  }
  def disconnect(): Unit = closeable.closeStream()
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
    val response = Json.decode(res.getBytes()).to[Result[Res, StreamStatus]].value
    response match {
      case Result.Success(value) => onMessage(value); matPromise.trySuccess(StreamStarted(() => closeable.closeStream()))
      case Result.Error(error)   => matPromise.trySuccess(error); disconnect()
    }
  }

  override val mat: Future[StreamStatus] = matPromise.future
}
