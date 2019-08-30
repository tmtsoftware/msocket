package msocket.impl.streaming

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Json}
import msocket.api.Result

import scala.concurrent.{Future, Promise}

abstract class ConnectedSource[Res, Mat] extends Source[Res, Mat] {
  def onTextMessage(res: String): Unit
  var onMessage: Res => Unit = println
  var closeable: Closeable = new Closeable {
    override def closeStream(): Unit = ()
  }
  def disconnect(): Unit = closeable.closeStream()
}

class SimpleConnectedSource[Res: Decoder] extends ConnectedSource[Res, NotUsed] {
  override def onTextMessage(res: String): Unit = {
    onMessage(Json.decode(res.getBytes()).to[Res].value)
  }

  override val mat: NotUsed = NotUsed
}

class ConnectedSourceWithErr[Res: Decoder, Err: Decoder] extends ConnectedSource[Res, Future[Option[Err]]] {
  private val matPromise: Promise[Option[Err]] = Promise()

  override def onTextMessage(res: String): Unit = {
    val response = Json.decode(res.getBytes()).to[Result[Res, Err]].value
    response match {
      case Result.Success(value) => onMessage(value); matPromise.trySuccess(None)
      case Result.Error(value)   => matPromise.trySuccess(Some(value)); disconnect()
    }
  }

  override val mat: Future[Option[Err]] = matPromise.future
}
