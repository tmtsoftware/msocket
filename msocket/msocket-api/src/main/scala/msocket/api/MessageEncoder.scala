package msocket.api

import io.bullet.borer.Encoder
import msocket.api.models.ServiceError

import scala.util.control.NonFatal

abstract class MessageEncoder[Req, M](implicit ep: ErrorProtocol[Req]) {
  def encode[Res: Encoder](response: Res): M

  lazy val errorEncoder: PartialFunction[Throwable, M] = {
    case NonFatal(ex: ep.E) => encode(ex)
    case NonFatal(ex)       => encode(ServiceError.fromThrowable(ex))
  }
}
