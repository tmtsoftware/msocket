package msocket.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.WebsocketClient

import scala.concurrent.Future

class WebsocketClientJs[Req: Encoder](baseUri: String) extends WebsocketClient[Req] {
  override def requestStream[Res: Decoder : Encoder](request: Req): Source[Res, NotUsed] = ???

  override def requestStreamWithError[Res: Decoder : Encoder, Err: Decoder : Encoder](request: Req): Source[Res, Future[Option[Err]]] = ???

  override def requestResponse[Res: Decoder : Encoder](request: Req): Future[Res] = ???
}
