package msocket.impl

import io.bullet.borer.Decoder
import msocket.api.Subscription

import scala.concurrent.Future

abstract class Connector[Req] {
  def requestResponse[Res: Decoder](req: Req): Future[Res]
  def requestStream[Res: Decoder](req: Req, onMessage: Res => Unit): Subscription
}
