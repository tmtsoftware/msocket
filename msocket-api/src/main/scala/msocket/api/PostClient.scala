package msocket.api

import io.bullet.borer.{Decoder, Encoder}

import scala.concurrent.Future

trait PostClient {
  def requestResponse[Req: Encoder, Res: Decoder](req: Req): Future[Res]
}
