package msocket.jvm.stream

import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.security.api.{AuthorizationPolicy, PassThroughPolicy}

import scala.concurrent.Future

trait StreamRequestHandler[Req] {
  def handle(request: Req): Future[StreamResponse]

  protected def future[Res: Encoder](result: Future[Res]): Future[StreamResponse] =
    stream(Source.future(result))

  protected def sFuture[Res: Encoder](policy: AuthorizationPolicy)(result: => Future[Res]): Future[StreamResponse] =
    sStream(policy)(Source.future(result))

  protected def stream[Res: Encoder](stream: Source[Res, Any]): Future[StreamResponse] =
    sStream(PassThroughPolicy)(stream)

  protected def sStream[Res: Encoder](policy: AuthorizationPolicy)(stream: => Source[Res, Any]): Future[StreamResponse] =
    Future.successful(StreamResponse.from(stream, policy))

}
