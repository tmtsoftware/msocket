package msocket.impl.streaming

import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Json}
import msocket.api.models._

class ConnectedSource[Res: Decoder, Mat] extends Source[Res, Subscription] {
  def onTextMessage(res: String): Unit = {
    val message = Json
      .decode(res.getBytes())
      .to[Res]
      .valueTry
      .getOrElse {
        subscription.cancel()
        throw Json.decode(res.getBytes()).to[MSocketException].value
      }
    onMessage(message)
  }
  private var onMessage: Res => Unit = x => ()
  var subscription: Subscription     = () => ()
  def foreach(f: Res => Unit): Unit = {
    onMessage = f
  }
  override val materializedValue: Subscription = subscription
}
