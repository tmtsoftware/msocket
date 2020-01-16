package msocket.api

import io.bullet.borer
import io.bullet.borer.Target

sealed abstract class ContentType(val mimeType: String, val target: Target)

object ContentType {
  case object Json extends ContentType("application/json", borer.Json)
  case object Cbor extends ContentType("application/cbor", borer.Cbor)

  def fromMimeType(mimeType: String): ContentType = mimeType match {
    case Json.mimeType => Json
    case Cbor.mimeType => Cbor
    case _             => throw new RuntimeException(s"unsupported mimeType: $mimeType")
  }
}
