package msocket.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.{ErrorProtocol, MessageEncoder, RequestHandler}

import scala.concurrent.Future

abstract class StreamHandler[Req: ErrorProtocol, M] extends MessageEncoder[Req, M] with RequestHandler[Req, Source[M, NotUsed]] {
  def stream[Res: Encoder, Mat](response: Source[Res, Mat]): Source[M, NotUsed] =
    response.map(encode[Res]).recover(errorEncoder).mapMaterializedValue(_ => NotUsed)

  def stream[Res: Encoder](input: Future[Res]): Source[M, NotUsed] = stream(Source.future(input))
}
