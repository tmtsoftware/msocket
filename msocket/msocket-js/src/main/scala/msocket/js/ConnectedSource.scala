package msocket.js

import org.apache.pekko.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{Subscription, Transport}
import msocket.portable.Observer

class ConnectedSource[Req, Res: Decoder: Encoder](req: Req, transport: Transport[Req]) extends Source[Res, Subscription] {
  private var observers: Seq[Observer[Res]]             = Nil
  override val subscription: Subscription               = transport.requestStream(req, Observer.combine(() => observers))
  override def onMessage(observer: Observer[Res]): Unit = observers :+= observer
}
