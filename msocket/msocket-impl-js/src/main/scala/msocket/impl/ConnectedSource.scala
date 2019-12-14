package msocket.impl

import akka.stream.scaladsl.Source
import io.bullet.borer.Decoder
import msocket.api.{Subscription, Transport}

class ConnectedSource[Req, Res: Decoder](req: Req, transport: Transport[Req]) extends Source[Res, Subscription] {
  private var onMessage: Res => Unit = x => ()

  override val materializedValue: Subscription = transport.requestStream(req, onMessage)

  def foreach(f: Res => Unit): Unit = {
    onMessage = f
  }
}
