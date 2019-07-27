package msocket.core.api

import akka.Done
import io.bullet.borer.Codec

trait DoneCodec {
  implicit lazy val doneCodec: Codec[Done] = Codec.implicitly[String].bimap[Done](_ => "done", _ => Done)
}
