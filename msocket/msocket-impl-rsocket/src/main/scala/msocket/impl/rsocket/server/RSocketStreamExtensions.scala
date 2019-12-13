package msocket.impl.rsocket.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import msocket.api.Encoding.CborByteBuffer
import msocket.impl.StreamExtensions

import scala.concurrent.{ExecutionContext, Future}

trait RSocketStreamExtensions extends StreamExtensions[Payload] {
  override def stream[T: Encoder, Mat](input: Source[T, Mat]): Source[Payload, NotUsed] = {
    input
      .map(x => DefaultPayload.create(CborByteBuffer.encode(x)))
      .mapMaterializedValue(_ => NotUsed)
  }

  def future[T: Encoder](input: Future[T])(implicit ec: ExecutionContext): Future[Payload] = {
    input.map(x => DefaultPayload.create(CborByteBuffer.encode(x)))
  }
}
