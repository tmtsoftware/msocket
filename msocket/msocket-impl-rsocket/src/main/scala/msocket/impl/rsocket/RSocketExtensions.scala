package msocket.impl.rsocket

import io.bullet.borer.{Decoder, Encoder}
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import msocket.api.ContentEncoding.{CborByteBuffer, JsonText}
import msocket.api.ContentType.{Cbor, Json}
import msocket.api.{ContentType, ErrorProtocol}

object RSocketExtensions {

  implicit class RSocketEncoding(contentType: ContentType) {
    def payload[T: Encoder](input: T): Payload = contentType match {
      case Json => DefaultPayload.create(JsonText.encode(input))
      case Cbor => DefaultPayload.create(CborByteBuffer.encode(input))
    }

    def response[Res: Decoder, Req: ErrorProtocol](payload: Payload): Res = contentType match {
      case Json => JsonText.decodeWithError[Res, Req](payload.getDataUtf8)
      case Cbor => CborByteBuffer.decodeWithError[Res, Req](payload.getData)
    }

    def request[Req: Decoder](payload: Payload): Req = contentType match {
      case Json => JsonText.decode[Req](payload.getDataUtf8)
      case Cbor => CborByteBuffer.decode[Req](payload.getData)
    }
  }

}
