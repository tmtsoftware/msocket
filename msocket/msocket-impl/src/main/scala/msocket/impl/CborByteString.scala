package msocket.impl

import akka.util.ByteString
import io.bullet.borer.compat.akka._
import msocket.api.ContentEncoding.CborBinary

case object CborByteString extends CborBinary[ByteString]
