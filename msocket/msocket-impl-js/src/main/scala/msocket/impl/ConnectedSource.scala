package msocket.impl

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{Subscription, Transport}

import scala.util.Try

class ConnectedSource[Req, Res: Decoder: Encoder](req: Req, transport: Transport[Req]) extends Source[Res, Subscription] {
  private var messageHandlers: Seq[Try[Option[Res]] => Unit] = Nil
  private def handleMessage(x: Try[Option[Res]]): Unit       = messageHandlers.foreach(f => f(x))

  override val subscription: Subscription                         = transport.requestStream(req, handleMessage)
  override def onMessage(handler: Try[Option[Res]] => Unit): Unit = messageHandlers :+= handler
}
