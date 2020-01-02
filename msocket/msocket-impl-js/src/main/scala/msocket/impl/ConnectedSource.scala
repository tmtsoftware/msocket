package msocket.impl

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{Subscription, Transport}

class ConnectedSource[Req, Res: Decoder: Encoder](req: Req, transport: Transport[Req]) extends Source[Res, Subscription] {
  private var effects: Seq[Res => Unit] = Nil

  private def onMessage(x: Res): Unit = effects.foreach(f => f(x))

  override val materializedValue: Subscription = transport.requestStream(req, onMessage)

  def subscribe(effect: Res => Unit): Unit = {
    effects :+= effect
  }
}
