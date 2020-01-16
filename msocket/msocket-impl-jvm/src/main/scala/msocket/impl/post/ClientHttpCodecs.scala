package msocket.impl.post

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import io.bullet.borer.Encoder
import msocket.api.ContentType

trait ClientHttpCodecs extends ServerHttpCodecs {
  def clientContentType: ContentType

  override implicit def borerToEntityMarshaller[T: Encoder]: ToEntityMarshaller[T] = clientContentType match {
    case ContentType.Json => borerJsonMarshaller()
    case ContentType.Cbor => borerCborMarshaller()
  }
}
