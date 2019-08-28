package msocket.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.RequestClient

import scala.concurrent.Future

class WebsocketClientJs[Req: Encoder](baseUri: String) extends RequestClient[Req] {
  override def requestStream[Res: Decoder](request: Req): Source[Res, NotUsed]                                    = ???
  override def requestStreamWithError[Res: Decoder, Err: Decoder](request: Req): Source[Res, Future[Option[Err]]] = ???
  override def requestResponse[Res: Decoder](request: Req): Future[Res]                                           = ???
}
