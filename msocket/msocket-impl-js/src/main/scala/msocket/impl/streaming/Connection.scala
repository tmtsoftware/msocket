package msocket.impl.streaming

import io.bullet.borer.Encoder

abstract class Connection[Req: Encoder] {
  def start(req: Req, source: ConnectedSource[_, _]): Closeable
}
