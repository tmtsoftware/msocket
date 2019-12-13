package msocket.impl.streaming

import io.bullet.borer.Decoder
import msocket.api.Subscription

abstract class Connector[Req] {
  def connect[Res: Decoder](req: Req, onMessage: Res => Unit): Subscription
}
