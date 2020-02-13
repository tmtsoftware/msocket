package msocket.api.utils

import java.nio.ByteBuffer

object ByteBufferExtensions {

  implicit class RichByteBuffer(val buf: ByteBuffer) extends AnyVal {
    def toByteArray: Array[Byte] = {
      val bytes = new Array[Byte](buf.remaining())
      buf.get(bytes)
      bytes
    }
  }

}
