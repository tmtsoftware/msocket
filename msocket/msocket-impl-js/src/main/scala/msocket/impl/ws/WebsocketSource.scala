package msocket.impl.ws

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder, Json}
import msocket.api.Result
import org.scalajs.dom.raw.WebSocket

import scala.concurrent.{ExecutionContext, Future, Promise}

abstract class WebsocketSource[Req: Encoder, Res: Decoder, Mat](implicit ec: ExecutionContext) extends Source[Res, Mat] {
  def uri: String
  var onMessage: Res => Unit = println
  def isOpen: Future[Unit]   = isOpenPromise.future

  def send(req: Req): Unit = {
    isOpen.foreach { _ =>
      webSocket.send(Json.encode(req).toUtf8String)
    }
  }

  def close(): Unit = webSocket.close()

  protected final val webSocket            = new WebSocket(uri)
  private val isOpenPromise: Promise[Unit] = Promise()

  webSocket.onopen = { _ =>
    isOpenPromise.success(())
    println("connection open")
  }

  webSocket.onclose = { _ =>
    println("connection closed")
  }
}

class WebsocketSourceWithErr[Req: Encoder, Res: Decoder, Err: Decoder](val uri: String)(implicit ec: ExecutionContext)
    extends WebsocketSource[Req, Res, Future[Option[Err]]] {

  private val matPromise: Promise[Option[Err]] = Promise()

  webSocket.onmessage = { messageEvent =>
    val str      = messageEvent.data.asInstanceOf[String]
    val response = Json.decode(str.getBytes()).to[Result[Res, Err]].value
    response match {
      case Result.Success(value) => onMessage(value); matPromise.trySuccess(None)
      case Result.Error(value)   => matPromise.trySuccess(Some(value)); close()
    }
  }

  override val mat: Future[Option[Err]] = matPromise.future

}

class SimpleWebsocketSource[Req: Encoder, Res: Decoder](val uri: String)(implicit ec: ExecutionContext)
    extends WebsocketSource[Req, Res, NotUsed] {

  webSocket.onmessage = { messageEvent =>
    val str      = messageEvent.data.asInstanceOf[String]
    val response = Json.decode(str.getBytes()).to[Res].value
    onMessage(response)
  }

  override val mat: NotUsed = NotUsed
}
