package msocket.impl

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{Subscription, Transport}

class ConnectedSource[Req, Res: Decoder: Encoder](req: Req, transport: Transport[Req]) extends Source[Res, Subscription] {
  private var messageHandlers: Seq[Res => Unit]     = Nil
  private var errorHandlers: Seq[Throwable => Unit] = Nil

  private def handleMessage(x: Res): Unit     = messageHandlers.foreach(f => f(x))
  private def handleError(x: Throwable): Unit = errorHandlers.foreach(f => f(x))

  override val materializedValue: Subscription = transport.requestStream(req, handleMessage, handleError)

  def onMessage(handler: Res => Unit): Unit = {
    messageHandlers :+= handler
  }

  def onError(handler: Throwable => Unit): Unit = {
    errorHandlers :+= handler
  }
}
