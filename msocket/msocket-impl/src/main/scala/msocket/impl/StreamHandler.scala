package msocket.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.{MessageEncoder, MessageHandler}

import scala.concurrent.Future

abstract class StreamHandler[Req, M](messageEncoder: MessageEncoder[Req, M]) extends MessageHandler[Req, Source[M, NotUsed]] {
  def stream[Res: Encoder, Mat](response: Source[Res, Mat]): Source[M, NotUsed] =
    response.map(messageEncoder.encode[Res]).recover(messageEncoder.errorEncoder).mapMaterializedValue(_ => NotUsed)

  def stream[Res: Encoder](input: Future[Res]): Source[M, NotUsed] = stream(Source.future(input))
}
