package msocket.impl

import io.bullet.borer.{Decoder, Encoder}
import msocket.api.PostClient

import scala.concurrent.Future

class PostClientJs(uri: String) extends PostClient {
  override def requestResponse[Req: Encoder, Res: Decoder](req: Req): Future[Res] = ???
}
