package msocket.impl

import akka.util.ByteString
import io.bullet.borer.compat.akka._
import msocket.api.Encoding.CborGeneric

case object CborByteString extends CborGeneric[ByteString]
