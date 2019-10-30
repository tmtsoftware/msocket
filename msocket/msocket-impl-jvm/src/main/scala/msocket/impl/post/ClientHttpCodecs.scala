package msocket.impl.post

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import io.bullet.borer.{Cbor, Encoder, Json, Target}

trait ClientHttpCodecs extends ServerHttpCodecs {
  def encoding: Target

  override implicit def borerToEntityMarshaller[T: Encoder]: ToEntityMarshaller[T] = encoding match {
    case Cbor => borerCborMarshaller()
    case Json => borerJsonMarshaller()
  }
}
