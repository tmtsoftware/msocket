package msocket.impl.post

import akka.NotUsed
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.api.models.FetchEvent
import msocket.api.{ErrorProtocol, MessageHandler}
import msocket.impl.ServerStreamingSupport

import scala.concurrent.duration.DurationLong

abstract class HttpPostHandler[Req: ErrorProtocol]
    extends ServerStreamingSupport[Req, FetchEvent](new FetchEventEncoder[Req])
    with MessageHandler[Req, Route] {

  override def stream[Res: Encoder, Mat](response: Source[Res, Mat]): Source[FetchEvent, NotUsed] =
    super.stream(response).keepAlive(30.seconds, () => FetchEvent.Heartbeat)
}
