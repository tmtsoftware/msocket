package msocket.impl

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.{Subscription, Transport}
import msocket.portable.Observer

import scala.util.Try

class ConnectedSource[Req, Res: Decoder: Encoder](req: Req, transport: Transport[Req]) extends Source[Res, Subscription] {
  private var observers: Seq[Observer[Res]]                = Nil
  private def handleMessage(input: Try[Option[Res]]): Unit = observers.foreach(obs => obs.on(input))
  override val subscription: Subscription                  = transport.requestStream(req, Observer.from(handleMessage))
  override def onMessage(observer: Observer[Res]): Unit    = observers :+= observer
}
