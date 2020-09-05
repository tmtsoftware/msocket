package msocket.impl.rsocket

import io.bullet.borer.{Decoder, Encoder}
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import msocket.api.ContentEncoding.JsonText
import msocket.api.ContentType.{Cbor, Json}
import msocket.api.models.ResponseHeaders
import msocket.api.{ContentType, ErrorProtocol}

object RSocketExtensions {

  implicit class RSocketEncoding(contentType: ContentType) {
    def payload[T: Encoder](input: T, headers: ResponseHeaders): Payload =
      contentType match {
        case Json => DefaultPayload.create(JsonText.encode(input), JsonText.encode(headers))
        case Cbor => DefaultPayload.create(CborByteBuffer.encode(input), CborByteBuffer.encode(headers))
      }

    def response[Res: Decoder, Req: ErrorProtocol](payload: Payload): Res = {
      contentType match {
        case Json =>
          val headers = JsonText.decode[ResponseHeaders](payload.getMetadataUtf8)
          JsonText.decodeFull(payload.getDataUtf8, headers.errorType)
        case Cbor =>
          val headers = CborByteBuffer.decode[ResponseHeaders](payload.getMetadata)
          CborByteBuffer.decodeFull(payload.getData, headers.errorType)
      }
    }

    def request[Req: Decoder](payload: Payload): Req =
      contentType match {
        case Json => JsonText.decode[Req](payload.getDataUtf8)
        case Cbor => CborByteBuffer.decode[Req](payload.getData)
      }
  }

}
