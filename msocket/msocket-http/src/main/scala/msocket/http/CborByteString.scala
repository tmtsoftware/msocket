package msocket.http

import org.apache.pekko.util.ByteString
import io.bullet.borer.compat.pekko._
import msocket.api.ContentEncoding.CborBinary

case object CborByteString extends CborBinary[ByteString]
