package msocket.core.client

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import io.bullet.borer.{Decoder, Encoder}

import scala.concurrent.Future

trait ClientSocket[Req] {
  def requestStream[Res: Decoder: Encoder](request: Req): Source[Res, NotUsed]

  def requestResponse[Res: Decoder: Encoder](request: Req)(implicit mat: Materializer): Future[Res] = {
    requestStream(request).runWith(Sink.head)
  }
}
