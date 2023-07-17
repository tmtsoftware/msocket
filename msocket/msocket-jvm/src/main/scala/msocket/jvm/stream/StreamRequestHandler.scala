package msocket.jvm.stream

import org.apache.pekko.stream.scaladsl.Source
import io.bullet.borer.Encoder
import msocket.security.api.AuthorizationPolicy
import msocket.security.models.AccessToken

import scala.concurrent.Future

trait StreamRequestHandler[Req] {
  def handle(request: Req): Future[StreamResponse]

  protected def response[Res: Encoder](result: Future[Res]): Future[StreamResponse] =
    stream(Source.future(result))

  protected def sResponse[Res: Encoder](policy: AuthorizationPolicy)(resultFactory: AccessToken => Future[Res]): Future[StreamResponse] =
    sStream(policy)(accessToken => Source.future(resultFactory(accessToken)))

  protected def stream[Res: Encoder](stream: Source[Res, Any]): Future[StreamResponse] =
    Future.successful(StreamResponse.from(_ => stream, None))

  protected def sStream[Res: Encoder](policy: AuthorizationPolicy)(streamFactory: AccessToken => Source[Res, Any]): Future[StreamResponse] =
    Future.successful(StreamResponse.from(streamFactory, Some(policy)))

}
