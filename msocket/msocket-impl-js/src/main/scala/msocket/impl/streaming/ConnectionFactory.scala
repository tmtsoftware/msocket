package msocket.impl.streaming

import io.bullet.borer.Encoder

abstract class ConnectionFactory[Req: Encoder] {
  def connect[S <: ConnectedSource[_, _]](req: Req, source: S): S
}
