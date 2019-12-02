package msocket.impl.rsocket.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import msocket.impl.Encoding.CborBinary
import msocket.impl.StreamExtensions

trait RSocketStreamExtensions extends StreamExtensions[Payload] {
  override def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[Payload, NotUsed] = {
    input
      .map(x => DefaultPayload.create(CborBinary.encode(x).asByteBuffer))
      .mapMaterializedValue(_ => NotUsed)
  }
}
